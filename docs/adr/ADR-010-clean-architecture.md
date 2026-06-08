# ADR-010: Clean Architecture Layering

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, backend standards

---

## Context

The project has complex business rules (UC-01 through UC-04) that involve
multiple collaborating domain objects, external services, and transaction
boundaries. Without a clear layer structure, business rules tend to leak
into controllers or repositories, making them untestable in isolation.

## Decision

Adopt **Clean Architecture** (Robert C. Martin) adapted for Spring Boot:

```
API layer (controllers, DTOs)
  → Application layer (use cases, application services)
    → Domain layer (entities, value objects, repository interfaces)
      ← Infrastructure layer (JPA repos, Redis adapters, HTTP clients)
```

Dependency rule: inner layers know nothing about outer layers.
Domain entities have zero Spring dependencies.
Application services depend on domain interfaces, not infrastructure implementations.

## Consequences

**Positive:**
- Use cases testable in pure unit tests (no Spring context, no database)
- Infrastructure swappable (e.g. swap Redis for Hazelcast) without touching business logic
- Clear place for every class — no "where does this go?" questions
- Academically defensible for thesis (DDD + Clean Architecture well-documented in literature)

**Negative / trade-offs:**
- More classes than a simple 3-layer MVC approach
- Indirection: a booking request touches 5–6 classes vs 2 in a simple approach

**Risks:**
- Over-engineering simple CRUD: for pure data-management endpoints (tenant setup, user admin)
  a lightweight approach (no use case class, direct service → repository) is acceptable
  and documented as an exception
