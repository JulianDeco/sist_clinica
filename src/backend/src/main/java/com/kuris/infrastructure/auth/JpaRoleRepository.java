package com.kuris.infrastructure.auth;

import com.kuris.domain.auth.Role;
import com.kuris.domain.auth.RoleRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Lookup de nombre de rol por id — usado solo en auth para emitir el claim `role` en el JWT. */
@Repository
public interface JpaRoleRepository extends JpaRepository<Role, UUID>, RoleRepository {

  @Override
  @Query("SELECT r.name FROM Role r WHERE r.id = :id")
  Optional<String> findNameById(@Param("id") UUID id);
}
