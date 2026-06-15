package com.clinicasaas.api.rbac;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TC-03 / TC-04 / TC-06 — integración de verificación de permisos vía Spring Security. Spec:
 * T-004-rbac-permission-check.spec.md
 *
 * <p>Requiere Testcontainers PostgreSQL + Redis (se configura en fase Green de TDD).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("TDD: not yet implemented — requiere Testcontainers Redis y endpoints T-006")
class PermissionIntegrationTest {

  @Autowired private MockMvc mockMvc;

  /**
   * TC-03: SECRETARY con JWT válido intenta acceder a endpoint protegido con ENCOUNTER_CREATE → 403
   * Forbidden.
   */
  @Test
  @Disabled("TDD: not yet implemented")
  void endpoint_ENCOUNTER_CREATE_rolSECRETARY_retorna403() throws Exception {
    // 1. Generar JWT con rol SECRETARY (helper de test a crear en fase Green)
    // 2. mockMvc.perform(post("/fhir/R4/Encounter").header("Authorization", "Bearer <token>"))
    //           .andExpect(status().isForbidden());
  }

  /** TC-04: ADMIN con JWT válido accede a endpoint protegido con TENANT_CONFIG → 200 OK. */
  @Test
  @Disabled("TDD: not yet implemented")
  void endpoint_TENANT_CONFIG_rolADMIN_retorna200() throws Exception {
    // 1. Generar JWT con rol ADMIN
    // 2. mockMvc.perform(get("/admin/tenant/config").header("Authorization", "Bearer <token>"))
    //           .andExpect(status().isOk());
  }

  /**
   * TC-06: PermissionService resuelve permisos del tenant correcto — userId del tenant A no hereda
   * permisos del tenant B.
   */
  @Test
  @Disabled("TDD: not yet implemented")
  void getPermissions_aislamientoTenant_noFiltrosCrossTenant() throws Exception {
    // 1. Crear user_tenant (userId, tenantA, roleId=SECRETARY)
    // 2. Intentar request con JWT de tenantB para ese userId
    // 3. Verificar que los permisos resueltos corresponden a tenantA, no a tenantB
  }
}
