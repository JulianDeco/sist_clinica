package com.kuris.infrastructure.auth;

import com.kuris.domain.auth.RefreshToken;
import com.kuris.domain.auth.RefreshTokenRepository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Adaptador JPA para RefreshTokenRepository. */
@Repository
public interface JpaRefreshTokenRepository
    extends JpaRepository<RefreshToken, UUID>, RefreshTokenRepository {

  /** SELECT FOR UPDATE — previene race condition en refresh concurrente (EC-05). */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :hash")
  Optional<RefreshToken> findByTokenHashForUpdate(@Param("hash") String tokenHash);

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
  List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId);
}
