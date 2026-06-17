package com.kuris.application.auth;

import com.kuris.api.auth.dto.*;
import jakarta.servlet.http.HttpServletResponse;

/** Contrato del caso de uso de autenticación (T-003). */
public interface AuthUseCase {

  /** FR-01: valida credenciales y devuelve identity token + lista de tenants. */
  LoginResponse login(LoginRequest request);

  /** FR-02: convierte identity token en session token; setea cookie refreshToken. */
  TokenResponse selectTenant(
      SelectTenantRequest request,
      String identityToken,
      HttpServletResponse response,
      String ipAddress,
      String userAgent);

  /** FR-03: emite nuevo session token para otro tenant; rota cookie refreshToken. */
  TokenResponse switchTenant(
      SwitchTenantRequest request,
      String accessToken,
      HttpServletResponse response,
      String ipAddress,
      String userAgent);

  /** FR-04: rota refresh token; devuelve nuevo access token. */
  TokenResponse refresh(
      String refreshTokenValue, HttpServletResponse response, String ipAddress, String userAgent);

  /** FR-05: añade JTI al blocklist y revoca el refresh token. */
  void logout(String accessToken, String refreshTokenValue, HttpServletResponse response);
}
