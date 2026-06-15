package com.clinicasaas.infrastructure.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Tests unitarios para PermissionCacheAdapter. Spec: T-004-rbac-permission-check.spec.md */
@ExtendWith(MockitoExtension.class)
class PermissionCacheAdapterTest {

  @Mock private StringRedisTemplate redis;
  @Mock private ValueOperations<String, String> valueOps;

  private PermissionCacheAdapter adapter;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new PermissionCacheAdapter(redis, new ObjectMapper(), 300);
  }

  @Test
  void getPermissions_clavePresente_retornaSetDeserializado() {
    given(redis.opsForValue()).willReturn(valueOps);
    String key = "clinica:perms:" + tenantId + ":" + userId;
    given(valueOps.get(key)).willReturn("[\"APPOINTMENT_READ\",\"PATIENT_READ\"]");

    Optional<Set<String>> result = adapter.getPermissions(tenantId, userId);

    assertThat(result).isPresent();
    assertThat(result.get()).containsExactlyInAnyOrder("APPOINTMENT_READ", "PATIENT_READ");
  }

  @Test
  void getPermissions_claveAusente_retornaEmpty() {
    given(redis.opsForValue()).willReturn(valueOps);
    given(valueOps.get(anyString())).willReturn(null);

    Optional<Set<String>> result = adapter.getPermissions(tenantId, userId);

    assertThat(result).isEmpty();
  }

  @Test
  void setPermissions_serializaYGuardaConTTL() {
    given(redis.opsForValue()).willReturn(valueOps);
    Set<String> perms = Set.of("APPOINTMENT_CREATE");

    adapter.setPermissions(tenantId, userId, perms);

    then(valueOps)
        .should()
        .set(
            eq("clinica:perms:" + tenantId + ":" + userId),
            anyString(),
            eq(java.time.Duration.ofSeconds(300)));
  }

  @Test
  void evictPermissions_eliminaClave() {
    adapter.evictPermissions(tenantId, userId);

    then(redis).should().delete("clinica:perms:" + tenantId + ":" + userId);
  }
}
