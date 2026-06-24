package com.kuris.config;

import io.jsonwebtoken.Claims;
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

  // WHY: mismo valor que el default placeholder en application.yml — si llega hasta aquí
  // significa que JWT_SECRET no fue seteado en el entorno (ADR-pendiente: fail-fast en boot).
  private static final String PLACEHOLDER_SECRET =
      "cambiar-en-produccion-minimo-64-caracteres-aleatorios-aqui-xxxxxxxxxxx";
  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey key;
  private final long accessTokenMs;

  public JwtConfig(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-token-expiration-ms}") long accessTokenMs) {
    validateSecret(secret);
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenMs = accessTokenMs;
  }

  private static void validateSecret(String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("app.jwt.secret no puede estar vacío");
    }
    if (PLACEHOLDER_SECRET.equals(secret)) {
      throw new IllegalStateException(
          "app.jwt.secret usa el valor placeholder por defecto — definir JWT_SECRET en el"
              + " entorno con un valor aleatorio real");
    }
    if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "app.jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " bytes para HS256");
    }
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

  public long getAccessTokenMs() {
    return accessTokenMs;
  }
}
