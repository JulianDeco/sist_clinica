# ClinicaSaaS — Documentación

SaaS multitenant FHIR R4 para clínicas pequeñas (1–5 profesionales) en Argentina.
Proyecto de tesis del Seminario de Trabajo Final UAI 2026.

## Stack

Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA ·
PostgreSQL 16 · Redis 8 · Angular 18 · Angular Material · Docker / Compose.

## Casos de uso core (ICONIX)

| CU | Descripción |
|---|---|
| CU-01 | Reservar turno con validación integral |
| CU-02 | Gestionar consulta médica (SOAP básico) |
| CU-03 | Calcular y notificar predicción de ausentismo |
| CU-04 | Sugerir y aplicar overbooking inteligente |

## Secciones

- **[Arquitectura](arquitectura/01-high-level-architecture.md)** — Descripción de alto nivel y capas
- **[Estándares](estandares/01-backend-standards.md)** — Backend, frontend, DB, seguridad, API, Git
- **[ADRs](adr/decisions-log.md)** — Architecture Decision Records
- **[Testing](testing/01-testing-standards.md)** — Estrategia TDD + SDD
- **[Specs](specs/T-003-auth-jwt.spec.md)** — Especificaciones de features
- **[Diagramas ICONIX](diagramas/index.md)** — Todos los diagramas Mermaid
- **[Seminario](seminario/plan-de-trabajo.md)** — Plan de trabajo, negocios, entregas UAI
