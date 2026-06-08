# Architecture Decision Records — ClinicaSaaS

ADRs document significant architectural decisions: what was decided, why,
and what trade-offs were accepted. They are immutable once merged — amend
by creating a new ADR that supersedes the previous one.

## Template

See [ADR-000-template.md](ADR-000-template.md) for the standard format.

## Index

| ID | Title | Status | Date |
|---|---|---|---|
| [ADR-001](ADR-001-spring-boot.md) | Spring Boot 3 as backend framework | Accepted | 2026-06-08 |
| [ADR-002](ADR-002-postgresql.md) | PostgreSQL 16 as primary database | Accepted | 2026-06-08 |
| [ADR-003](ADR-003-row-level-multitenancy.md) | Row-level multitenancy over schema-per-tenant | Accepted | 2026-06-08 |
| [ADR-004](ADR-004-redis.md) | Redis for cache and ephemeral state only | Accepted | 2026-06-08 |
| [ADR-005](ADR-005-angular.md) | Angular 18 as frontend framework | Accepted | 2026-06-08 |
| [ADR-006](ADR-006-jwt-authentication.md) | JWT stateless authentication with refresh rotation | Accepted | 2026-06-08 |
| [ADR-007](ADR-007-docker.md) | Docker + Docker Compose for deployment | Accepted | 2026-06-08 |
| [ADR-008](ADR-008-flyway.md) | Flyway for database migrations | Accepted | 2026-06-08 |
| [ADR-009](ADR-009-fhir-jsonb-storage.md) | FHIR resources stored as JSONB in PostgreSQL | Accepted | 2026-06-08 |
| [ADR-010](ADR-010-clean-architecture.md) | Clean Architecture layering | Accepted | 2026-06-08 |
| [ADR-011](ADR-011-spec-driven-development.md) | Spec-Driven Development as mandatory workflow | Accepted | 2026-06-08 |
| [ADR-012](ADR-012-llm-integration.md) | IA generativa mediante Claude API para asistencia clínica | Accepted | 2026-06-08 |
| [ADR-013](ADR-013-notification-channels.md) | Abstracción de canales de notificación — Telegram MVP, WhatsApp post-piloto | Accepted | 2026-06-08 |
