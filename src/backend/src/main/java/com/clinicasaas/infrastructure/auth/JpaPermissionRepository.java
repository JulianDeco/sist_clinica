package com.clinicasaas.infrastructure.auth;

import com.clinicasaas.domain.auth.Permission;
import com.clinicasaas.domain.auth.PermissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Consulta permisos por rol navegando la relación @ManyToMany de Role (V002). */
@Repository
public interface JpaPermissionRepository
    extends JpaRepository<Permission, UUID>, PermissionRepository {

  @Override
  @Query("SELECT p.name FROM Role r JOIN r.permissions p WHERE r.id = :roleId")
  List<String> findNamesByRoleId(@Param("roleId") UUID roleId);
}
