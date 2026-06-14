package com.clinicasaas.api.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Body de POST /api/v1/auth/select-tenant. */
public record SelectTenantRequest(@NotNull UUID tenantId) {}
