package com.clinicasaas.infrastructure.cache;

import com.clinicasaas.application.auth.PermissionCachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Adaptador Redis para PermissionCachePort. Clave: clinica:perms:{tenantId}:{userId} · TTL
 * configurable (default 300 s).
 */
@Service
public class PermissionCacheAdapter implements PermissionCachePort {

  private static final Logger log = LoggerFactory.getLogger(PermissionCacheAdapter.class);
  private static final String KEY_PREFIX = "clinica:perms:";
  private static final TypeReference<Set<String>> SET_TYPE = new TypeReference<>() {};

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;
  private final long ttlSeconds;

  public PermissionCacheAdapter(
      StringRedisTemplate redis,
      ObjectMapper objectMapper,
      @Value("${app.rbac.cache-ttl-seconds:300}") long ttlSeconds) {
    this.redis = redis;
    this.objectMapper = objectMapper;
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public Optional<Set<String>> getPermissions(UUID tenantId, UUID userId) {
    String value = redis.opsForValue().get(key(tenantId, userId));
    if (value == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(value, SET_TYPE));
    } catch (JsonProcessingException e) {
      log.warn(
          "Error deserializando permisos de Redis para {}/{}: {}",
          tenantId,
          userId,
          e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void setPermissions(UUID tenantId, UUID userId, Set<String> permissions) {
    try {
      String value = objectMapper.writeValueAsString(permissions);
      redis.opsForValue().set(key(tenantId, userId), value, Duration.ofSeconds(ttlSeconds));
    } catch (JsonProcessingException e) {
      log.warn(
          "Error serializando permisos para Redis {}/{}: {}", tenantId, userId, e.getMessage());
    }
  }

  @Override
  public void evictPermissions(UUID tenantId, UUID userId) {
    redis.delete(key(tenantId, userId));
  }

  private String key(UUID tenantId, UUID userId) {
    return KEY_PREFIX + tenantId + ":" + userId;
  }
}
