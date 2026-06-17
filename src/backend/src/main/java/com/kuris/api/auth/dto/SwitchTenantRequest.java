package com.kuris.api.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Body de POST /api/v1/auth/switch-tenant. */
public record SwitchTenantRequest(@NotNull UUID tenantId) {}
