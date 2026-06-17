package com.kuris.domain.auth;

import java.util.Optional;
import java.util.UUID;

/** Puerto de dominio para consultas de roles (T-003/T-004). */
public interface RoleRepository {

  /** Devuelve el nombre del rol para emitir el claim {@code role} en el JWT. */
  Optional<String> findNameById(UUID id);
}
