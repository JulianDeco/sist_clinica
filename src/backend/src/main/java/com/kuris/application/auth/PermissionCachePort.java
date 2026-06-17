package com.kuris.application.auth;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Puerto de caché para permisos de usuario por tenant. */
public interface PermissionCachePort {

  /** Retorna los permisos cacheados, o empty si no hay entrada. */
  Optional<Set<String>> getPermissions(UUID tenantId, UUID userId);

  /** Almacena los permisos con TTL configurado. */
  void setPermissions(UUID tenantId, UUID userId, Set<String> permissions);

  /** Invalida la entrada de caché para el usuario en el tenant dado. */
  void evictPermissions(UUID tenantId, UUID userId);
}
