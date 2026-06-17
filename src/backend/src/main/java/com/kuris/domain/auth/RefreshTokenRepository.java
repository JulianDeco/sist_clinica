package com.kuris.domain.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de refresh tokens. */
public interface RefreshTokenRepository {
  RefreshToken save(RefreshToken token);

  /** SELECT FOR UPDATE — previene race condition en refresh concurrente (EC-05). */
  Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

  List<RefreshToken> findActiveByUserId(UUID userId);

  List<RefreshToken> saveAll(List<RefreshToken> tokens);
}
