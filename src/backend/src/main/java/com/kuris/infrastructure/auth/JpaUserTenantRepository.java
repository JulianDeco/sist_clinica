package com.kuris.infrastructure.auth;

import com.kuris.domain.auth.UserTenant;
import com.kuris.domain.auth.UserTenantId;
import com.kuris.domain.auth.UserTenantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Adaptador JPA para UserTenantRepository. */
@Repository
public interface JpaUserTenantRepository
    extends JpaRepository<UserTenant, UserTenantId>, UserTenantRepository {

  @Query("SELECT ut FROM UserTenant ut WHERE ut.id.userId = :userId AND ut.active = true")
  List<UserTenant> findActiveByUserId(@Param("userId") UUID userId);

  @Query(
      "SELECT ut FROM UserTenant ut WHERE ut.id.userId = :userId AND ut.tenantId = :tenantId AND ut.active = true")
  Optional<UserTenant> findActiveByUserIdAndTenantId(
      @Param("userId") UUID userId, @Param("tenantId") UUID tenantId);
}
