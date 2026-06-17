package com.kuris.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.kuris.api.auth.dto.*;
import com.kuris.config.JwtConfig;
import com.kuris.domain.auth.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

// LENIENT: algunos tests fallan antes de usar stubbings de setUp (activeUser)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthUseCaseImplTest {

  @Mock UserRepository userRepo;
  @Mock UserTenantRepository userTenantRepo;
  @Mock RefreshTokenRepository refreshTokenRepo;
  @Mock RoleRepository roleRepo;
  @Mock TokenBlocklistPort blocklist;
  @Mock JwtConfig jwtConfig;
  @Mock PasswordEncoder passwordEncoder;
  @Mock HttpServletResponse httpResponse;

  AuthUseCaseImpl useCase;

  UUID userId;
  UUID tenantId;
  User activeUser;

  @BeforeEach
  void setUp() throws Exception {
    useCase =
        new AuthUseCaseImpl(
            userRepo,
            userTenantRepo,
            refreshTokenRepo,
            roleRepo,
            jwtConfig,
            blocklist,
            passwordEncoder,
            604_800_000L);
    userId = UUID.randomUUID();
    tenantId = UUID.randomUUID();
    activeUser = buildUserMock(userId, "admin@test.com", "$2a$12$hash", true);

    given(httpResponse.getWriter()).willReturn(new PrintWriter(new StringWriter()));
  }

  // ── TC-01 ─────────────────────────────────────────────────────────────────
  @Test
  void login_validCredentials_returnsIdentityTokenAndTenants() {
    given(userRepo.findByEmail("admin@test.com")).willReturn(Optional.of(activeUser));
    given(passwordEncoder.matches("pass", "$2a$12$hash")).willReturn(true);
    UserTenant membership = buildMembershipMock(userId, tenantId);
    given(userTenantRepo.findActiveByUserId(userId)).willReturn(List.of(membership));
    given(roleRepo.findNameById(membership.getRoleId())).willReturn(Optional.of("ADMIN"));
    given(jwtConfig.generateIdentityToken(userId)).willReturn("identity.token");

    LoginResponse resp = useCase.login(new LoginRequest("admin@test.com", "pass"));

    assertThat(resp.identityToken()).isEqualTo("identity.token");
    assertThat(resp.tenants()).hasSize(1);
    assertThat(resp.tenants().get(0).tenantId()).isEqualTo(tenantId);
    assertThat(resp.tenants().get(0).role()).isEqualTo("ADMIN");
  }

  // ── TC-02 ─────────────────────────────────────────────────────────────────
  @Test
  void login_wrongPassword_throws401() {
    given(userRepo.findByEmail("admin@test.com")).willReturn(Optional.of(activeUser));
    given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

    assertThatThrownBy(() -> useCase.login(new LoginRequest("admin@test.com", "wrong")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  // ── TC-02b ────────────────────────────────────────────────────────────────
  @Test
  void login_unknownEmail_throws401() {
    given(userRepo.findByEmail(anyString())).willReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.login(new LoginRequest("nobody@test.com", "pass")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  // ── TC-03 ─────────────────────────────────────────────────────────────────
  @Test
  void selectTenant_validToken_returnsAccessTokenAndBlocksIdentityJti() {
    Claims claims = buildIdentityTokenClaims(userId, "jti-1", future(300));
    given(jwtConfig.parseAndValidate("identity.token")).willReturn(claims);
    given(blocklist.isBlocked("jti-1")).willReturn(false);
    UserTenant membership = buildMembershipMock(userId, tenantId);
    given(userTenantRepo.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(membership));
    given(roleRepo.findNameById(membership.getRoleId())).willReturn(Optional.of("ADMIN"));
    given(jwtConfig.generateAccessToken(eq(userId), eq(tenantId), eq("ADMIN")))
        .willReturn("access.token");
    given(refreshTokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

    TokenResponse result =
        useCase.selectTenant(
            new SelectTenantRequest(tenantId),
            "identity.token",
            httpResponse,
            "127.0.0.1",
            "agent");

    assertThat(result.accessToken()).isEqualTo("access.token");
    then(blocklist).should().blockJti(eq("jti-1"), any()); // single-use (AC-12)
  }

  // ── TC-04 ─────────────────────────────────────────────────────────────────
  @Test
  void selectTenant_expiredToken_throws401() {
    given(jwtConfig.parseAndValidate("expired.token"))
        .willThrow(new ExpiredJwtException(null, null, "expired"));

    assertThatThrownBy(
            () ->
                useCase.selectTenant(
                    new SelectTenantRequest(tenantId), "expired.token", httpResponse, "ip", "ua"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  // ── TC-05 ─────────────────────────────────────────────────────────────────
  @Test
  void selectTenant_unauthorizedTenant_throws403() {
    Claims claims = buildIdentityTokenClaims(userId, "jti-x", future(300));
    given(jwtConfig.parseAndValidate("identity.token")).willReturn(claims);
    given(blocklist.isBlocked("jti-x")).willReturn(false);
    given(userTenantRepo.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                useCase.selectTenant(
                    new SelectTenantRequest(tenantId), "identity.token", httpResponse, "ip", "ua"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN));
  }

  // ── TC-06 ─────────────────────────────────────────────────────────────────
  @Test
  void refresh_validToken_rotatesAndReturnsNewAccessToken() {
    RefreshToken stored = buildRefreshToken(userId, tenantId, false);
    given(refreshTokenRepo.findByTokenHashForUpdate(any())).willReturn(Optional.of(stored));
    UserTenant membership = buildMembershipMock(userId, tenantId);
    given(userTenantRepo.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(membership));
    given(roleRepo.findNameById(membership.getRoleId())).willReturn(Optional.of("ADMIN"));
    given(jwtConfig.generateAccessToken(eq(userId), eq(tenantId), eq("ADMIN")))
        .willReturn("new.access.token");
    given(refreshTokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

    TokenResponse result = useCase.refresh("valid-raw-token", httpResponse, "ip", "ua");

    assertThat(result.accessToken()).isEqualTo("new.access.token");
    assertThat(stored.isRevoked()).isTrue(); // token anterior revocado
  }

  // ── TC-07 ─────────────────────────────────────────────────────────────────
  @Test
  void refresh_revokedToken_revokesAllAndThrows401WithReuseAttack() {
    RefreshToken revoked = buildRefreshToken(userId, tenantId, true);
    given(refreshTokenRepo.findByTokenHashForUpdate(any())).willReturn(Optional.of(revoked));
    given(refreshTokenRepo.findActiveByUserId(userId)).willReturn(List.of());

    assertThatThrownBy(() -> useCase.refresh("revoked-token", httpResponse, "ip", "ua"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e -> {
              assertThat(((ResponseStatusException) e).getStatusCode())
                  .isEqualTo(HttpStatus.UNAUTHORIZED);
              assertThat(((ResponseStatusException) e).getReason()).isEqualTo("REUSE_ATTACK");
            });
  }

  // ── TC-08 ─────────────────────────────────────────────────────────────────
  @Test
  void logout_blocksJtiAndRevokesRefreshToken() {
    Claims claims = buildAccessTokenClaims(userId, tenantId, "jti-logout", future(1800));
    given(jwtConfig.parseAndValidate("access.token")).willReturn(claims);
    RefreshToken rt = buildRefreshToken(userId, tenantId, false);
    given(refreshTokenRepo.findByTokenHashForUpdate(any())).willReturn(Optional.of(rt));
    given(refreshTokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

    useCase.logout("access.token", "raw-refresh-token", httpResponse);

    then(blocklist).should().blockJti(eq("jti-logout"), any());
    assertThat(rt.isRevoked()).isTrue();
  }

  // ── TC-11 ─────────────────────────────────────────────────────────────────
  @Test
  void switchTenant_blocksOldJtiAndReturnsNewTokenForNewTenant() {
    UUID newTenantId = UUID.randomUUID();
    Claims claims = buildAccessTokenClaims(userId, tenantId, "jti-old", future(1800));
    given(jwtConfig.parseAndValidate("access.token")).willReturn(claims);
    given(blocklist.isBlocked("jti-old")).willReturn(false);
    UserTenant membership = buildMembershipMock(userId, newTenantId);
    given(userTenantRepo.findActiveByUserIdAndTenantId(userId, newTenantId))
        .willReturn(Optional.of(membership));
    given(roleRepo.findNameById(membership.getRoleId())).willReturn(Optional.of("DOCTOR"));
    given(jwtConfig.generateAccessToken(eq(userId), eq(newTenantId), eq("DOCTOR")))
        .willReturn("new.tenant.token");
    given(refreshTokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

    TokenResponse result =
        useCase.switchTenant(
            new SwitchTenantRequest(newTenantId), "access.token", httpResponse, "ip", "ua");

    assertThat(result.accessToken()).isEqualTo("new.tenant.token");
    then(blocklist).should().blockJti(eq("jti-old"), any());
  }

  // ── TC-15 ─────────────────────────────────────────────────────────────────
  @Test
  void selectTenant_alreadyUsedIdentityToken_throws401() {
    Claims claims = buildIdentityTokenClaims(userId, "jti-used", future(300));
    given(jwtConfig.parseAndValidate("used.token")).willReturn(claims);
    given(blocklist.isBlocked("jti-used")).willReturn(true); // ya usado

    assertThatThrownBy(
            () ->
                useCase.selectTenant(
                    new SelectTenantRequest(tenantId), "used.token", httpResponse, "ip", "ua"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  // ── TC-16 ─────────────────────────────────────────────────────────────────
  @Test
  void refresh_emitsAccessTokenWithTenantIdStoredInRefreshToken() {
    UUID storedTenant = UUID.randomUUID();
    RefreshToken stored = buildRefreshToken(userId, storedTenant, false);
    given(refreshTokenRepo.findByTokenHashForUpdate(any())).willReturn(Optional.of(stored));
    UserTenant membership = buildMembershipMock(userId, storedTenant);
    given(userTenantRepo.findActiveByUserIdAndTenantId(userId, storedTenant))
        .willReturn(Optional.of(membership));
    given(roleRepo.findNameById(membership.getRoleId())).willReturn(Optional.of("ADMIN"));
    given(jwtConfig.generateAccessToken(eq(userId), eq(storedTenant), eq("ADMIN")))
        .willReturn("tenant-specific.token");
    given(refreshTokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

    TokenResponse result = useCase.refresh("raw-token", httpResponse, "ip", "ua");

    assertThat(result.accessToken()).isEqualTo("tenant-specific.token");
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private User buildUserMock(UUID id, String email, String hash, boolean active) {
    User u = mock(User.class);
    lenient().when(u.getId()).thenReturn(id);
    lenient().when(u.getEmail()).thenReturn(email);
    lenient().when(u.getPasswordHash()).thenReturn(hash);
    lenient().when(u.isActive()).thenReturn(active);
    return u;
  }

  /** Construye el membership ANTES de pasarlo a given() para evitar UnfinishedStubbing. */
  private UserTenant buildMembershipMock(UUID userId, UUID tenantId) {
    UUID roleId = UUID.randomUUID();
    UserTenant ut = mock(UserTenant.class);
    lenient().when(ut.getTenantId()).thenReturn(tenantId);
    lenient().when(ut.getRoleId()).thenReturn(roleId);
    return ut;
  }

  private Claims buildIdentityTokenClaims(UUID userId, String jti, long expMs) {
    Claims c = mock(Claims.class);
    lenient().when(c.getSubject()).thenReturn(userId.toString());
    lenient().when(c.getId()).thenReturn(jti);
    lenient().when(c.get("purpose")).thenReturn("tenant-select");
    lenient().when(c.getExpiration()).thenReturn(new Date(expMs));
    return c;
  }

  private Claims buildAccessTokenClaims(UUID userId, UUID tenantId, String jti, long expMs) {
    Claims c = mock(Claims.class);
    lenient().when(c.getSubject()).thenReturn(userId.toString());
    lenient().when(c.getId()).thenReturn(jti);
    lenient().when(c.get("tenant_id")).thenReturn(tenantId.toString());
    lenient().when(c.get("role")).thenReturn("ADMIN");
    lenient().when(c.getExpiration()).thenReturn(new Date(expMs));
    return c;
  }

  private RefreshToken buildRefreshToken(UUID userId, UUID tenantId, boolean revoked) {
    RefreshToken rt =
        RefreshToken.create(
            userId,
            tenantId,
            UUID.randomUUID().toString(),
            "hash",
            OffsetDateTime.now().plusDays(7),
            "ip",
            "ua");
    if (revoked) rt.revoke("TEST");
    return rt;
  }

  private long future(long seconds) {
    return System.currentTimeMillis() + seconds * 1000;
  }
}
