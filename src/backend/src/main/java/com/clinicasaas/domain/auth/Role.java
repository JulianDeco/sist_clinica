package com.clinicasaas.domain.auth;

import jakarta.persistence.*;
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

  protected Role() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
