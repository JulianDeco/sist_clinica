package com.clinicasaas.application.auth;

import com.clinicasaas.api.auth.dto.*;
import com.clinicasaas.config.JwtConfig;
import com.clinicasaas.domain.auth.*;
import com.clinicasaas.infrastructure.cache.JwtBlocklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implementación del caso de uso de autenticación (T-003). Flujo two-step: identity token →
 * select-tenant → session token + refresh cookie.
 */
@Service
@Transactional
public class AuthUseCaseImpl implements AuthUseCase {

  private static final String COOKIE_NAME = "refreshToken";
  private static final String COOKIE_PATH = "/api/v1/auth";
  private static final int REFRESH_TOKEN_TTL_DAYS = 7;

  private final UserRepository userRepo;
  private final UserTenantRepository userTenantRepo;
  private final RefreshTokenRepository refreshTokenRepo;
  private final RoleRepository roleRepo;
  private final JwtConfig jwtConfig;
  private final JwtBlocklistService blocklist;
  private final PasswordEncoder passwordEncoder;
  private final long refreshTokenMs;

  public AuthUseCaseImpl(
      UserRepository userRepo,
      UserTenantRepository userTenantRepo,
      RefreshTokenRepository refreshTokenRepo,
      RoleRepository roleRepo,
      JwtConfig jwtConfig,
      JwtBlocklistService blocklist,
      PasswordEncoder passwordEncoder,
      @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenMs) {
    this.userRepo = userRepo;
    this.userTenantRepo = userTenantRepo;
    this.refreshTokenRepo = refreshTokenRepo;
    this.roleRepo = roleRepo;
    this.jwtConfig = jwtConfig;
    this.blocklist = blocklist;
    this.passwordEncoder = passwordEncoder;
    this.refreshTokenMs = refreshTokenMs;
  }

  /** FR-01: login → identity token + lista de tenants. */
  @Override
  public LoginResponse login(LoginRequest request) {
    User user =
        userRepo
            .findByEmail(request.email())
            .filter(User::isActive)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    List<UserTenant> memberships = userTenantRepo.findActiveByUserId(user.getId());
    List<TenantInfo> tenants =
        memberships.stream()
            .map(ut -> new TenantInfo(ut.getTenantId(), resolveRoleName(ut.getRoleId())))
            .collect(Collectors.toList());

    String identityToken = jwtConfig.generateIdentityToken(user.getId());
    return new LoginResponse(identityToken, tenants);
  }

  /** FR-02: select-tenant → session token + refresh cookie (identity token single-use). */
  @Override
  public TokenResponse selectTenant(
      SelectTenantRequest request,
      String identityToken,
      HttpServletResponse response,
      String ipAddress,
      String userAgent) {
    Claims claims = parseIdentityToken(identityToken);
    UUID userId = UUID.fromString(claims.getSubject());
    String jti = claims.getId();

    // Single-use: bloquear el identity token una vez usado (AC-12)
    long remainingSec =
        Math.max(0, (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
    blocklist.blockJti(jti, Duration.ofSeconds(remainingSec));

    UserTenant membership = requireMembership(userId, request.tenantId());
    String roleName = resolveRoleName(membership.getRoleId());

    String accessToken = jwtConfig.generateAccessToken(userId, request.tenantId(), roleName);
    issueRefreshCookie(userId, request.tenantId(), response, ipAddress, userAgent);
    return new TokenResponse(accessToken);
  }

  /** FR-03: switch-tenant → nuevo session token + rotación de refresh cookie. */
  @Override
  public TokenResponse switchTenant(
      SwitchTenantRequest request,
      String accessToken,
      HttpServletResponse response,
      String ipAddress,
      String userAgent) {
    Claims claims = parseAccessToken(accessToken);
    UUID userId = UUID.fromString(claims.getSubject());

    // Revocar JTI actual
    long remainingSec =
        Math.max(0, (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
    blocklist.blockJti(claims.getId(), Duration.ofSeconds(remainingSec));

    UserTenant membership = requireMembership(userId, request.tenantId());
    String roleName = resolveRoleName(membership.getRoleId());

    String newAccessToken = jwtConfig.generateAccessToken(userId, request.tenantId(), roleName);
    issueRefreshCookie(userId, request.tenantId(), response, ipAddress, userAgent);
    return new TokenResponse(newAccessToken);
  }

  /** FR-04: refresh → nuevo access token + rotación de refresh cookie (single-use rotation). */
  @Override
  public TokenResponse refresh(
      String refreshTokenValue, HttpServletResponse response, String ipAddress, String userAgent) {
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_MISSING");
    }

    String hash = sha256(refreshTokenValue);
    RefreshToken stored =
        refreshTokenRepo
            .findByTokenHashForUpdate(hash)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED"));

    if (stored.isRevoked()) {
      // Reuse attack: revocar toda la familia (AC-07)
      revokeAllForUser(stored.getUserId(), "REUSE_ATTACK");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REUSE_ATTACK");
    }
    if (stored.isExpired()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED");
    }

    // Rotar: revocar el actual, emitir nuevo
    stored.revoke("ROTATED");
    refreshTokenRepo.save(stored);

    UUID userId = stored.getUserId();
    UUID tenantId = stored.getTenantId();
    UserTenant membership = requireMembership(userId, tenantId);
    String roleName = resolveRoleName(membership.getRoleId());

    String newAccessToken = jwtConfig.generateAccessToken(userId, tenantId, roleName);
    issueRefreshCookie(userId, tenantId, response, ipAddress, userAgent);
    return new TokenResponse(newAccessToken);
  }

  /** FR-05: logout → JTI en blocklist + revocación de refresh token + borrar cookie. */
  @Override
  public void logout(String accessToken, String refreshTokenValue, HttpServletResponse response) {
    try {
      Claims claims = jwtConfig.parseAndValidate(accessToken);
      long remainingSec =
          Math.max(0, (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
      blocklist.blockJti(claims.getId(), Duration.ofSeconds(remainingSec));
    } catch (JwtException ignored) {
      // Token ya expirado — continuar con revocación del refresh
    }

    if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
      String hash = sha256(refreshTokenValue);
      refreshTokenRepo
          .findByTokenHashForUpdate(hash)
          .ifPresent(
              rt -> {
                rt.revoke("LOGOUT");
                refreshTokenRepo.save(rt);
              });
    }

    clearRefreshCookie(response);
  }

  // ── helpers privados ──────────────────────────────────────────────────────

  private Claims parseIdentityToken(String token) {
    Claims claims;
    try {
      claims = jwtConfig.parseAndValidate(token);
    } catch (JwtException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
    if (!"tenant-select".equals(claims.get("purpose"))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
    if (blocklist.isBlocked(claims.getId())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
    return claims;
  }

  private Claims parseAccessToken(String token) {
    try {
      Claims claims = jwtConfig.parseAndValidate(token);
      if (blocklist.isBlocked(claims.getId())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
      }
      return claims;
    } catch (JwtException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
  }

  private UserTenant requireMembership(UUID userId, UUID tenantId) {
    return userTenantRepo
        .findActiveByUserIdAndTenantId(userId, tenantId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "TENANT_ACCESS_DENIED"));
  }

  private void issueRefreshCookie(
      UUID userId,
      UUID tenantId,
      HttpServletResponse response,
      String ipAddress,
      String userAgent) {
    String rawToken = generateSecureToken();
    String hash = sha256(rawToken);
    OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(REFRESH_TOKEN_TTL_DAYS);

    RefreshToken rt =
        RefreshToken.create(
            userId, tenantId, UUID.randomUUID().toString(), hash, expiresAt, ipAddress, userAgent);
    refreshTokenRepo.save(rt);

    Cookie cookie = new Cookie(COOKIE_NAME, rawToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath(COOKIE_PATH);
    cookie.setMaxAge(REFRESH_TOKEN_TTL_DAYS * 24 * 3600);
    // SameSite=Strict — Spring Boot no tiene setter directo, se usa header
    response.addCookie(cookie);
    response.addHeader(
        "Set-Cookie",
        COOKIE_NAME
            + "="
            + rawToken
            + "; Path="
            + COOKIE_PATH
            + "; HttpOnly; Secure; SameSite=Strict; Max-Age="
            + (REFRESH_TOKEN_TTL_DAYS * 24 * 3600));
  }

  private void clearRefreshCookie(HttpServletResponse response) {
    response.addHeader(
        "Set-Cookie",
        COOKIE_NAME + "=; Path=" + COOKIE_PATH + "; HttpOnly; Secure; SameSite=Strict; Max-Age=0");
  }

  private void revokeAllForUser(UUID userId, String reason) {
    List<RefreshToken> active = refreshTokenRepo.findActiveByUserId(userId);
    active.forEach(rt -> rt.revoke(reason));
    refreshTokenRepo.saveAll(active);
  }

  private static String generateSecureToken() {
    byte[] bytes = new byte[64];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** Resuelve el nombre del rol desde el roleId. T-004 añadirá cache Redis sobre esto. */
  private String resolveRoleName(UUID roleId) {
    return roleRepo.findNameById(roleId).orElse("UNKNOWN");
  }
}
