package com.clinicasaas.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.clinicasaas.domain.auth.UserTenant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** TC-01 / TC-02 / TC-05 / TC-07 / TC-08 / TC-09 Spec: T-004-rbac-permission-check.spec.md */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

  @Mock private com.clinicasaas.domain.auth.PermissionRepository permissionRepository;
  @Mock private com.clinicasaas.application.auth.PermissionCachePort permissionCachePort;
  @Mock private com.clinicasaas.domain.auth.UserTenantRepository userTenantRepository;
  @Mock private UserTenant userTenant;

  private PermissionService permissionService;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final UUID roleId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    permissionService =
        new PermissionService(permissionRepository, permissionCachePort, userTenantRepository);
  }

  // ── TC-01 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_cacheVacia_consultaBDYPoblaRedis() {
    given(permissionCachePort.getPermissions(tenantId, userId)).willReturn(Optional.empty());
    given(userTenantRepository.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(userTenant));
    given(userTenant.getRoleId()).willReturn(roleId);
    given(permissionRepository.findNamesByRoleId(roleId))
        .willReturn(List.of("APPOINTMENT_READ", "APPOINTMENT_CREATE"));

    Set<String> result = permissionService.getPermissions(tenantId, userId);

    assertThat(result).containsExactlyInAnyOrder("APPOINTMENT_READ", "APPOINTMENT_CREATE");
    then(permissionCachePort).should().setPermissions(tenantId, userId, result);
  }

  // ── TC-02 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_cachePopulada_noConsultaBD() {
    given(permissionCachePort.getPermissions(tenantId, userId))
        .willReturn(Optional.of(Set.of("APPOINTMENT_READ")));

    Set<String> result = permissionService.getPermissions(tenantId, userId);

    assertThat(result).containsExactly("APPOINTMENT_READ");
    then(permissionRepository).shouldHaveNoInteractions();
    then(userTenantRepository).shouldHaveNoInteractions();
  }

  // ── TC-05 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_luegoDEvict_reconsultaBD() {
    given(permissionCachePort.getPermissions(tenantId, userId)).willReturn(Optional.empty());
    given(userTenantRepository.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(userTenant));
    given(userTenant.getRoleId()).willReturn(roleId);
    given(permissionRepository.findNamesByRoleId(roleId)).willReturn(List.of("PATIENT_READ"));

    permissionService.evictPermissions(tenantId, userId);
    Set<String> result = permissionService.getPermissions(tenantId, userId);

    then(permissionCachePort).should().evictPermissions(tenantId, userId);
    assertThat(result).containsExactly("PATIENT_READ");
    then(permissionCachePort).should().setPermissions(tenantId, userId, result);
  }

  // ── TC-07 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_sinUserTenantEntry_retornaSetVacio() {
    given(permissionCachePort.getPermissions(tenantId, userId)).willReturn(Optional.empty());
    given(userTenantRepository.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.empty());

    Set<String> result = permissionService.getPermissions(tenantId, userId);

    assertThat(result).isEmpty();
    then(permissionRepository).shouldHaveNoInteractions();
  }

  // ── TC-08 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_rolSinPermisos_retornaSetVacio() {
    given(permissionCachePort.getPermissions(tenantId, userId)).willReturn(Optional.empty());
    given(userTenantRepository.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(userTenant));
    given(userTenant.getRoleId()).willReturn(roleId);
    given(permissionRepository.findNamesByRoleId(roleId)).willReturn(List.of());

    Set<String> result = permissionService.getPermissions(tenantId, userId);

    assertThat(result).isEmpty();
  }

  // ── TC-09 ─────────────────────────────────────────────────────────────────

  @Test
  void getPermissions_redisTimeout_fallbackABD() {
    given(permissionCachePort.getPermissions(tenantId, userId))
        .willThrow(new org.springframework.data.redis.RedisConnectionFailureException("timeout"));
    given(userTenantRepository.findActiveByUserIdAndTenantId(userId, tenantId))
        .willReturn(Optional.of(userTenant));
    given(userTenant.getRoleId()).willReturn(roleId);
    given(permissionRepository.findNamesByRoleId(roleId)).willReturn(List.of("APPOINTMENT_READ"));

    Set<String> result = permissionService.getPermissions(tenantId, userId);

    assertThat(result).containsExactly("APPOINTMENT_READ");
  }
}
