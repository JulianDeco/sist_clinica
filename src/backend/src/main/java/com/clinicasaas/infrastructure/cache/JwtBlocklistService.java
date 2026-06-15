package com.clinicasaas.infrastructure.cache;

import com.clinicasaas.application.auth.TokenBlocklistPort;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Adaptador Redis para TokenBlocklistPort. Claves: clinica:jti:{jti} · clinica:ratelimit:login:{ip}
 * (estándar 04-redis-standards).
 */
@Service
public class JwtBlocklistService implements TokenBlocklistPort {

  private static final String JTI_PREFIX = "clinica:jti:";
  private static final String RATELIMIT_PREFIX = "clinica:ratelimit:login:";
  private static final int LOGIN_MAX_ATTEMPTS = 5;
  private static final long LOGIN_WINDOW_SEC = 60;

  private final StringRedisTemplate redis;

  public JwtBlocklistService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  /** Añade un JTI al blocklist con TTL = tiempo restante del token. */
  public void blockJti(String jti, Duration ttl) {
    redis.opsForValue().set(JTI_PREFIX + jti, "1", ttl);
  }

  /** Retorna true si el JTI está bloqueado (revocado). */
  public boolean isBlocked(String jti) {
    return Boolean.TRUE.equals(redis.hasKey(JTI_PREFIX + jti));
  }

  /**
   * Incrementa el contador de intentos de login para la IP dada. Retorna true si se supera el
   * límite (429 — FR-09).
   */
  public boolean isRateLimited(String ip) {
    String key = RATELIMIT_PREFIX + ip;
    Long count = redis.opsForValue().increment(key);
    if (count != null && count == 1) {
      redis.expire(key, Duration.ofSeconds(LOGIN_WINDOW_SEC));
    }
    return count != null && count > LOGIN_MAX_ATTEMPTS;
  }
}
