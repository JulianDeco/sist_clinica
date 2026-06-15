package com.clinicasaas.application.auth;

import java.time.Duration;

/** Puerto de dominio para blocklist de JTIs y rate limiting de login. */
public interface TokenBlocklistPort {

  /** Añade un JTI al blocklist con TTL = tiempo restante del token. */
  void blockJti(String jti, Duration ttl);

  /** Retorna true si el JTI está bloqueado (revocado o single-use consumido). */
  boolean isBlocked(String jti);

  /**
   * Incrementa el contador de intentos de login para la IP dada. Retorna true si se supera el
   * límite (FR-09).
   */
  boolean isRateLimited(String ip);
}
