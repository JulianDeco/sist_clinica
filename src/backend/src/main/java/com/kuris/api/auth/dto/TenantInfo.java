package com.kuris.api.auth.dto;

import java.util.UUID;

/** Tenant al que pertenece el usuario, devuelto tras login. */
public record TenantInfo(UUID tenantId, String role) {}
