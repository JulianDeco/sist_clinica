# Architecture Decision Records — Kuris

## ¿Qué es un ADR?

Un **Architecture Decision Record** es un documento corto que captura una decisión
arquitectónica significativa: el contexto que la motivó, lo que se decidió, las
alternativas que se consideraron y los trade-offs que se aceptaron.

Su valor no está en documentar lo que se hizo, sino en preservar el **por qué**.
Sin ADRs, cuando alguien (o un agente) encuentra una elección que le parece subóptima,
no sabe si es una limitación consciente, un error histórico, o una deuda técnica
pendiente. El ADR responde esa pregunta sin necesidad de revisar el historial de git
ni interrumpir a quien tomó la decisión.

## Reglas de uso

- **Inmutabilidad:** un ADR aceptado no se modifica. Si una decisión cambia, se crea
  un ADR nuevo con estado `Supersedes: ADR-XXX`.
- **Cuándo crear uno:** ante cualquier elección no obvia que afecte la estructura del
  sistema, el stack tecnológico, o una convención que otros colaboradores deban seguir.
- **Cuándo NO crear uno:** para decisiones de implementación reversibles o locales
  a una función (esas van como comentario `// WHY:` en el código).
- **Formato:** usar el [template](ADR-000-template.md). Un ADR bien escrito cabe en
  una pantalla.

## Estados posibles

| Estado | Significado |
|---|---|
| `Proposed` | En discusión, aún no aplicado |
| `Accepted` | Vigente, se aplica en el código actual |
| `Deprecated` | Ya no aplica, pero no fue reemplazado formalmente |
| `Superseded` | Reemplazado por otro ADR (se indica cuál) |

## Índice

| ID | Título | Estado | Fecha |
|---|---|---|---|
| [ADR-001](ADR-001-spring-boot.md) | Spring Boot 3 como framework backend | Accepted | 2026-06-08 |
| [ADR-002](ADR-002-postgresql.md) | PostgreSQL 16 como base de datos principal | Accepted | 2026-06-08 |
| [ADR-003](ADR-003-row-level-multitenancy.md) | Multitenancy por fila (row-level) en lugar de esquema por tenant | Accepted | 2026-06-08 |
| [ADR-004](ADR-004-redis.md) | Redis solo para caché y estado efímero | Accepted | 2026-06-08 |
| [ADR-005](ADR-005-angular.md) | Angular 18 como framework frontend | Accepted | 2026-06-08 |
| [ADR-006](ADR-006-jwt-authentication.md) | Autenticación JWT stateless con rotación de refresh token | Accepted | 2026-06-08 |
| [ADR-007](ADR-007-docker.md) | Docker + Docker Compose para despliegue | Accepted | 2026-06-08 |
| [ADR-008](ADR-008-flyway.md) | Flyway como único mecanismo de migraciones de BD | Accepted | 2026-06-08 |
| [ADR-009](ADR-009-fhir-jsonb-storage.md) | Recursos FHIR almacenados como JSONB en PostgreSQL | Accepted | 2026-06-08 |
| [ADR-010](ADR-010-clean-architecture.md) | Capas de Clean Architecture (domain / application / infrastructure / api) | Accepted | 2026-06-08 |
| [ADR-011](ADR-011-spec-driven-development.md) | Spec-Driven Development como workflow obligatorio | Accepted | 2026-06-08 |
| [ADR-012](ADR-012-llm-integration.md) | IA generativa mediante Claude API para asistencia clínica | Accepted | 2026-06-08 |
| [ADR-013](ADR-013-notification-channels.md) | Abstracción de canales de notificación — Telegram MVP, WhatsApp post-piloto | Accepted | 2026-06-08 |
| [ADR-014](ADR-014-multi-tenant-membership.md) | Membresía multitenant — identidad separada de la pertenencia a clínica | Accepted | 2026-06-09 |
| [ADR-015](ADR-015-user-practitioner-mapping.md) | Mapeo Usuario–Profesional — vínculo opcional y explícito | Accepted | 2026-06-10 |
| [ADR-016](ADR-016-appointment-encounter-state-machines.md) | Máquinas de estado separadas para Appointment y Encounter | Accepted | 2026-06-15 |
