# ADR-008: Flyway for Database Migrations

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, database

---

## Context

Database schema changes need version control with a repeatable,
auditable process. Spring Boot's JPA `ddl-auto=create` or `update`
is dangerous in production — it can silently drop columns or fail.

## Decision

Use **Flyway** as the sole schema migration mechanism.

`spring.jpa.hibernate.ddl-auto=validate` (never `create` or `update`).
Flyway runs on application startup before Spring context is fully initialized.
Migration files live in `src/main/resources/db/migration/V{N}__{desc}.sql`.

## Consequences

**Positive:**
- Schema history tracked in `flyway_schema_history` table
- Failed migration halts application startup — prevents running against wrong schema
- SQL migrations (not Java) — readable, reviewable, no ORM abstraction

**Negative / trade-offs:**
- Community edition: no out-of-the-box rollback — rollback must be a new migration
- Developer must never modify a committed migration file

**Risks:**
- Large migrations on production data: must be tested against a data volume
  representative of production before deploying
