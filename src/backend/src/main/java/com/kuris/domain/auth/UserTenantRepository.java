package com.kuris.domain.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de membresías usuario-tenant. */
public interface UserTenantRepository {
  List<UserTenant> findActiveByUserId(UUID userId);

  Optional<UserTenant> findActiveByUserIdAndTenantId(UUID userId, UUID tenantId);
}
