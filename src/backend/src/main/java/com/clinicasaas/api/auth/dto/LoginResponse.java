package com.clinicasaas.api.auth.dto;

import java.util.List;

/** Respuesta de POST /api/v1/auth/login. */
public record LoginResponse(String identityToken, List<TenantInfo> tenants) {}
