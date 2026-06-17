package com.kuris.domain.auth;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

/** Rol global del sistema (ADMIN, DOCTOR, SECRETARY) — tabla roles (V002). */
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private List<Permission> permissions;

  protected Role() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Permission> getPermissions() {
    return permissions;
  }
}
