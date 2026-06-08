# ADR-009: FHIR Resources Stored as JSONB in PostgreSQL

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, FHIR module

---

## Context

FHIR R4 defines ~150 resource types with variable structure and extensions.
Modeling each as a relational table would require a migration for every new
resource type and cannot handle the extensibility of FHIR profiles.

## Decision

Store FHIR resources as **JSONB** in a single `fhir_resources` table.
Extract queryable search parameters into a companion `fhir_search_params`
table (relational) for efficient lookups without JSONB full-scans.

Domain-critical attributes (e.g. `appointment_date`, `patient_id`) are
duplicated into typed columns on domain tables for JOIN performance.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| One table per FHIR resource type | Migration per new resource; cannot handle FHIR extensions |
| Pure JSONB (no search params table) | Full-scan on every search; unacceptable performance |
| Dedicated FHIR server (HAPI FHIR) | Over-engineered for MVP; adds another service on a VPS with 4GB RAM |

## Consequences

**Positive:**
- Supports all FHIR R4 resource types without schema changes
- GIN index on `resource_data` enables efficient JSONB queries
- Pattern identical to production FHIR servers (HAPI FHIR, Azure API for FHIR)
- `fhir_search_params` provides efficient indexed search on extracted values

**Negative / trade-offs:**
- JSONB updates rewrite the entire document (acceptable for clinical record size)
- FHIR validation must be done in application code, not enforced by schema

**Risks:**
- Accidental JSONB full-scan if developer forgets to use search params table:
  mitigated by code review and EXPLAIN ANALYZE requirement for new queries
