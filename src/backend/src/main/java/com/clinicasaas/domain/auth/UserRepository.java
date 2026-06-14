package com.clinicasaas.domain.auth;

import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de usuarios. */
public interface UserRepository {
  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);
}
