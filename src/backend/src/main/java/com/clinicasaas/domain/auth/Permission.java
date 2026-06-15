package com.clinicasaas.domain.auth;

import jakarta.persistence.*;
import java.util.UUID;

/** Permiso granular del sistema — tabla permissions (V002). */
@Entity
@Table(name = "permissions")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false, length = 50)
  private String module;

  @Column(length = 255)
  private String description;

  protected Permission() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getModule() {
    return module;
  }
}
