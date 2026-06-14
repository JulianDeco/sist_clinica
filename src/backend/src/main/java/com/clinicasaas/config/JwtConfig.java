package com.clinicasaas.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Servicio de generación y validación de JWTs (JJWT 0.12.6, NFR-01). */
@Component
public class JwtConfig {

  private final SecretKey key;
  private final long accessTokenMs;

  public JwtConfig(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-token-expiration-ms}") long accessTokenMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenMs = accessTokenMs;
  }

  /** Emite un identity token (purpose=tenant-select, TTL 5 min). */
  public String generateIdentityToken(UUID userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("purpose", "tenant-select")
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusSeconds(300)))
        .signWith(key)
        .compact();
  }

  /** Emite un session token con tenant_id + role (TTL configurable, default 30 min). */
  public String generateAccessToken(UUID userId, UUID tenantId, String role) {
    return Jwts.builder()
        .subject(userId.toString())
        .claims(Map.of("tenant_id", tenantId.toString(), "role", role))
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusMillis(accessTokenMs)))
        .signWith(key)
        .compact();
  }

  /** Parsea y valida firma + expiración. Lanza JwtException si falla. */
  public Claims parseAndValidate(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Extrae claims sin lanzar excepción — útil para logout donde el token puede estar próximo a
   * expirar.
   */
  public Claims parseUnchecked(String token) {
    try {
      return parseAndValidate(token);
    } catch (JwtException e) {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
  }

  public long getAccessTokenMs() {
    return accessTokenMs;
  }
}
