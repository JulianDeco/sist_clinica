# ADR-002: PostgreSQL 16 as Primary Database

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation

---

## Context

ClinicaSaaS needs a database that supports: JSONB for FHIR resources,
UUID primary keys, row-level multitenancy (tenant_id filtering), full
ACID transactions, GIN indexes for JSONB search, and compliance with
Argentine health data privacy law (Ley 25.326).

## Decision

Use **PostgreSQL 16** as the sole primary database.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| MySQL 8 | No native JSONB type — FHIR storage would require separate document store or complex schema |
| MongoDB | No ACID multi-document transactions — critical for atomic appointment booking (slot + appointment + coverage in one transaction) |
| MariaDB | Same JSONB limitation as MySQL |
| H2 (for tests) | Not production-equivalent; Testcontainers with real PostgreSQL preferred |

## Consequences

**Positive:**
- JSONB with GIN index: FHIR resources searchable without full-scan
- Native UUID type: primary keys + FK constraints without conversion
- ACID transactions: atomic booking (UC-01) is safe
- Row-Level Security (PostgreSQL RLS): additional layer for tenant isolation
- Ley 25.326 compliance: data isolation between tenants provable via query plans

**Negative / trade-offs:**
- More configuration than SQLite for local dev (mitigated: Docker Compose)
- Flyway migrations required (no auto-schema creation)

**Risks:**
- VPS constraint: `shared_buffers` must be capped at 128MB to stay within
  512MB container memory limit
