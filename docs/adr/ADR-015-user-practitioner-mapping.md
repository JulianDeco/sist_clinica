# ADR-015: Vínculo Usuario ↔ Practitioner por membresía

**Status**: PROPOSED (pendiente aprobación @julian)
**Date**: 2026-06-10
**Author**: Julián Deco
**Relates to**: ADR-003 (row-level multitenancy), ADR-009 (FHIR JSONB), ADR-014 (multi-tenant membership), T-005, T-006

---

## Context

CU-02 (gestionar consulta) y CU-01 (agenda) requieren que un usuario con rol
DOCTOR opere sobre *su propia* agenda y *sus* consultas. El rol en el JWT
(`role=DOCTOR`) autoriza la acción, pero no identifica *qué* `Practitioner`
FHIR es ese usuario: ni `users` ni `user_tenants` referencian al recurso
`Practitioner` almacenado en `fhir_resources` (JSONB, tenant-scoped).

Sin ese vínculo no se puede responder "mostrame mi agenda de hoy" sin
heurísticas frágiles (match por nombre/email dentro del JSONB).

Detectado durante el modelado de dominio ICONIX (T-020): la asociación
`Usuario 0..1 — 0..1 Profesional "actúa como"` existe en el modelo de dominio
pero no tiene soporte en el esquema físico.

---

## Decision

Agregar a `user_tenants` una columna opcional:

```sql
ALTER TABLE user_tenants
    ADD COLUMN practitioner_fhir_id UUID NULL;
```

- **Por membresía, no por usuario**: los recursos FHIR son tenant-scoped
  (ADR-003/009) — la misma persona es un recurso `Practitioner` distinto en
  cada clínica donde atiende. El vínculo vive donde vive la pertenencia.
- **NULL para usuarios no clínicos** (SECRETARY, ADMIN sin actividad
  asistencial).
- Integridad: FK lógica hacia `fhir_resources.id` validada en capa de
  aplicación (no FK física — `fhir_resources` guarda todos los resource types
  y la restricción "debe ser un Practitioner del mismo tenant" no es
  expresable como FK simple). Validación en el use case que asigna el vínculo.
- La migración (V012 o siguiente disponible) se implementa junto con T-005
  (FHIR core), que es cuando nace el recurso `Practitioner`.

---

## Alternatives Considered

| Alternativa | Por qué se descarta |
|---|---|
| Columna en `users` | Rompe el scoping por tenant: un usuario tendría el mismo Practitioner en todas las clínicas |
| Tabla de mapeo separada `user_practitioners` | Mismo grano que `user_tenants` (user × tenant) — tabla extra sin semántica nueva |
| Identifier dentro del JSONB del Practitioner apuntando al user | Invierte la dirección de la consulta más frecuente; requiere índice JSONB adicional y deja el vínculo fuera del modelo relacional de membresía |
| Resolver por email match en runtime | Frágil (emails distintos por clínica), sin integridad, costo por request |

---

## Consequences

**Positivas:**
- "Mi agenda / mis consultas" se resuelve con un join directo
  `user_tenants.practitioner_fhir_id` → recursos del tenant.
- El modelo físico queda alineado con el modelo de dominio ICONIX
  (Usuario "actúa como" Profesional, por clínica).
- Sin tablas nuevas; extiende la entidad de membresía existente (ADR-014).

**Negativas / trade-offs:**
- Integridad referencial en aplicación, no en BD (FK lógica).
- El alta de un médico requiere un paso extra: crear el Practitioner FHIR y
  vincularlo a la membresía (flujo de onboarding de T-005).

**Riesgos:**
- Vínculo desactualizado si se elimina el Practitioner → la baja lógica de
  recursos FHIR debe limpiar o invalidar el vínculo (regla en T-005).
