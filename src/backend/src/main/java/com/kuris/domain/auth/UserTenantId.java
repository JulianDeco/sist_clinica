package com.kuris.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Clave compuesta de user_tenants. */
@Embeddable
public class UserTenantId implements Serializable {

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "tenant_id")
  private UUID tenantId;

  protected UserTenantId() {}

  public UserTenantId(UUID userId, UUID tenantId) {
    this.userId = userId;
    this.tenantId = tenantId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserTenantId that)) return false;
    return Objects.equals(userId, that.userId) && Objects.equals(tenantId, that.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, tenantId);
  }
}
