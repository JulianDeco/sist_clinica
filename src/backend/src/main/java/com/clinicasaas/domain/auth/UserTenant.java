package com.clinicasaas.domain.auth;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Membresía de un usuario en un tenant con un rol (ADR-014, V010). */
@Entity
@Table(name = "user_tenants")
public class UserTenant {

  @EmbeddedId private UserTenantId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "tenant_id", insertable = false, updatable = false)
  private UUID tenantId;

  @Column(name = "role_id", nullable = false)
  private UUID roleId;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;

  protected UserTenant() {}

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getRoleId() {
    return roleId;
  }

  public boolean isActive() {
    return active;
  }

  public User getUser() {
    return user;
  }
}
