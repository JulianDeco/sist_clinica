package com.clinicasaas.domain.auth;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Token de refresco almacenado como SHA-256 hash (NFR-03). Sesión vinculada a tenant (V011). */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false, unique = true, length = 36)
  private String jti;

  @Column(name = "token_hash", nullable = false)
  private String tokenHash;

  @Column(name = "issued_at", nullable = false)
  private OffsetDateTime issuedAt;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  @Column(name = "revocation_reason", length = 100)
  private String revocationReason;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  protected RefreshToken() {}

  /** Factory method para crear un nuevo refresh token. */
  public static RefreshToken create(
      UUID userId,
      UUID tenantId,
      String jti,
      String tokenHash,
      OffsetDateTime expiresAt,
      String ipAddress,
      String userAgent) {
    RefreshToken rt = new RefreshToken();
    rt.userId = userId;
    rt.tenantId = tenantId;
    rt.jti = jti;
    rt.tokenHash = tokenHash;
    rt.issuedAt = OffsetDateTime.now();
    rt.expiresAt = expiresAt;
    rt.ipAddress = ipAddress;
    rt.userAgent = userAgent;
    return rt;
  }

  public void revoke(String reason) {
    this.revokedAt = OffsetDateTime.now();
    this.revocationReason = reason;
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired() {
    return OffsetDateTime.now().isAfter(expiresAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getJti() {
    return jti;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
}
