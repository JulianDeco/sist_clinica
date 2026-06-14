package com.clinicasaas.api.auth;

import com.clinicasaas.api.auth.dto.*;
import com.clinicasaas.application.auth.AuthUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Endpoints de autenticación (T-003). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String REFRESH_COOKIE = "refreshToken";

  private final AuthUseCase authUseCase;

  public AuthController(AuthUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  /** FR-01: autenticar identidad → identity token + lista de tenants. */
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authUseCase.login(request);
  }

  /** FR-02: seleccionar tenant → session token + cookie refreshToken. */
  @PostMapping("/select-tenant")
  public TokenResponse selectTenant(
      @Valid @RequestBody SelectTenantRequest request,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    String identityToken = extractBearer(authHeader);
    return authUseCase.selectTenant(
        request,
        identityToken,
        httpResponse,
        extractIp(httpRequest),
        httpRequest.getHeader("User-Agent"));
  }

  /** FR-03: cambiar de tenant → nuevo session token + cookie rotada. */
  @PostMapping("/switch-tenant")
  public TokenResponse switchTenant(
      @Valid @RequestBody SwitchTenantRequest request,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    String accessToken = extractBearer(authHeader);
    return authUseCase.switchTenant(
        request,
        accessToken,
        httpResponse,
        extractIp(httpRequest),
        httpRequest.getHeader("User-Agent"));
  }

  /** FR-04: rotar refresh token → nuevo access token. */
  @PostMapping("/refresh")
  public TokenResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    String refreshToken = extractCookie(httpRequest, REFRESH_COOKIE);
    return authUseCase.refresh(
        refreshToken, httpResponse, extractIp(httpRequest), httpRequest.getHeader("User-Agent"));
  }

  /** FR-05: logout → revocar tokens, eliminar cookie. */
  @DeleteMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    String accessToken = extractBearer(authHeader);
    String refreshToken = extractCookie(httpRequest, REFRESH_COOKIE);
    authUseCase.logout(accessToken, refreshToken, httpResponse);
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private String extractBearer(String header) {
    if (header != null && header.startsWith("Bearer ")) return header.substring(7);
    return "";
  }

  private String extractCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return null;
    for (Cookie c : request.getCookies()) {
      if (name.equals(c.getName())) return c.getValue();
    }
    return null;
  }

  private String extractIp(HttpServletRequest request) {
    // WHY: igual que RateLimitFilter — usar X-Real-IP (sobrescrito por Nginx) evita spoofing.
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) return realIp.trim();
    return request.getRemoteAddr();
  }
}
