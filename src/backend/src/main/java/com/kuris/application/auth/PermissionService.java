package com.kuris.application.auth;

import com.kuris.domain.auth.PermissionRepository;
import com.kuris.domain.auth.UserTenant;
import com.kuris.domain.auth.UserTenantRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

/**
 * Resuelve permisos de un usuario en un tenant: Redis → BD → Redis. Clave de caché:
 * clinica:perms:{tenantId}:{userId} (ADR-004, NFR-02).
 */
@Service
public class PermissionService {

  private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

  private final PermissionRepository permissionRepository;
  private final PermissionCachePort permissionCachePort;
  private final UserTenantRepository userTenantRepository;

  public PermissionService(
      PermissionRepository permissionRepository,
      PermissionCachePort permissionCachePort,
      UserTenantRepository userTenantRepository) {
    this.permissionRepository = permissionRepository;
    this.permissionCachePort = permissionCachePort;
    this.userTenantRepository = userTenantRepository;
  }

  /**
   * Retorna el conjunto de nombres de permiso del usuario en el tenant. Orden: caché Redis → BD. Si
   * Redis falla, hace fallback a BD con log WARN.
   */
  public Set<String> getPermissions(UUID tenantId, UUID userId) {
    Optional<Set<String>> cached = tryGetFromCache(tenantId, userId);
    if (cached.isPresent()) {
      return cached.get();
    }
    return loadFromDb(tenantId, userId);
  }

  /** Invalida la entrada de caché para el usuario en el tenant. */
  public void evictPermissions(UUID tenantId, UUID userId) {
    permissionCachePort.evictPermissions(tenantId, userId);
  }

  private Optional<Set<String>> tryGetFromCache(UUID tenantId, UUID userId) {
    try {
      return permissionCachePort.getPermissions(tenantId, userId);
    } catch (RedisConnectionFailureException ex) {
      log.warn(
          "Redis no disponible al leer permisos ({}/{}), fallback a BD: {}",
          tenantId,
          userId,
          ex.getMessage());
      return Optional.empty();
    }
  }

  private Set<String> loadFromDb(UUID tenantId, UUID userId) {
    UUID roleId =
        userTenantRepository
            .findActiveByUserIdAndTenantId(userId, tenantId)
            .map(UserTenant::getRoleId)
            .orElse(null);

    if (roleId == null) {
      return Set.of();
    }

    Set<String> perms = new HashSet<>(permissionRepository.findNamesByRoleId(roleId));
    trySetCache(tenantId, userId, perms);
    return perms;
  }

  private void trySetCache(UUID tenantId, UUID userId, Set<String> perms) {
    try {
      permissionCachePort.setPermissions(tenantId, userId, perms);
    } catch (RedisConnectionFailureException ex) {
      log.warn(
          "Redis no disponible al escribir permisos ({}/{}): {}",
          tenantId,
          userId,
          ex.getMessage());
    }
  }
}
