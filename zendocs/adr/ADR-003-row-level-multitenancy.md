# ADR-003: Row-Level Multitenancy over Schema-per-Tenant

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, UC-01, UC-02, UC-03, UC-04

---

## Context

Kuris is a multitenant SaaS platform. Every tenant (clinic) must be
fully isolated: a tenant must never see another tenant's patients,
appointments, or clinical records.

Two main strategies exist: schema-per-tenant (separate PostgreSQL schema
per clinic) and row-level (all tenants in shared tables, filtered by
`tenant_id` column).

## Decision

Use **row-level multitenancy**: all tenant-scoped tables include a
`tenant_id UUID NOT NULL` column. Every query that reads or writes
tenant data must include `WHERE tenant_id = ?`.

Enforcement mechanism: `TenantContextFilter` populates a ThreadLocal
from the JWT `tenant_id` claim. `TenantAwareRepository` base class
automatically applies the filter to all derived queries.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| Schema-per-tenant | VPS 4GB RAM: N separate connection pools (one per schema) unsustainable for 5–20 tenants. Flyway migrations require iterating all schemas. |
| Database-per-tenant | Impractical on a single VPS — requires N PostgreSQL instances or N databases with separate credentials |

## Consequences

**Positive:**
- Single connection pool — efficient on constrained VPS
- Flyway: `flyway migrate` applies to all tenants in one pass
- Simpler operations: one backup, one database to monitor
- Intelligence / reporting queries can aggregate across tenants for platform analytics (with explicit permission)

**Negative / trade-offs:**
- A bug that omits `WHERE tenant_id = ?` would leak cross-tenant data
  — mitigated by: `TenantAwareRepository` base class, mandatory integration test for tenant isolation per repository

**Risks:**
- Developer error is the primary risk. Mitigation: code review checklist
  includes explicit item "tenant_id present in all queries"

## Notes

PostgreSQL Row-Level Security (RLS) can be added as an additional safety
layer in a future ADR (requires a separate DB role per tenant). For MVP,
application-level filtering is sufficient and simpler to manage.
