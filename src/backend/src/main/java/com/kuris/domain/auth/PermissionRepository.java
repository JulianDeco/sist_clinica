package com.kuris.domain.auth;

import java.util.List;
import java.util.UUID;

/** Puerto de dominio para consulta de permisos por rol. */
public interface PermissionRepository {

  /** Retorna los nombres de permiso asignados al rol dado vía role_permissions. */
  List<String> findNamesByRoleId(UUID roleId);
}
