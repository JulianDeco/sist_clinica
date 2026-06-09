# ADR-001: Spring Boot 3 as Backend Framework

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation

---

## Context

ClinicaSaaS requires a backend that handles FHIR R4 CRUD, JWT authentication,
multitenant data isolation, scheduled jobs (notification reminders), and
integration with external services (WhatsApp Business API, email SMTP).

The previous stack (FastAPI / Python) was evaluated and discarded in favor
of a more complete enterprise framework better suited for the domain's
complexity and the team's long-term maintainability requirements.

## Decision

Use **Spring Boot 3.x** on **Java 21** as the backend application framework.

Spring Boot 3 provides: Spring Security (auth, RBAC), Spring Data JPA
(ORM + query DSL), Spring Scheduler (notification jobs), Spring Actuator
(health, metrics), and mature ecosystem for every integration needed.

Java 21 Virtual Threads (Project Loom) enable high concurrency for
notification dispatch without blocking threads or introducing reactive
complexity.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| FastAPI (Python) | Previously used; discarded — lacks mature RBAC library, async SQLAlchemy complexity for multitenancy, less enterprise-grade exception handling pattern |
| Quarkus | Smaller ecosystem, less team familiarity, fewer Spring Security equivalents for fine-grained RBAC |
| Micronaut | Similar ecosystem gap; GraalVM native image benefits not critical for this VPS deployment |

## Consequences

**Positive:**
- Spring Security provides production-grade JWT + RBAC out of the box
- Spring Data JPA eliminates most boilerplate for multitenant queries
- Spring Scheduler covers UC-03 (notification job) natively
- Mature, well-documented — academic references available for thesis
- JVM startup time acceptable (< 10s) with Docker healthcheck

**Negative / trade-offs:**
- Higher memory footprint than FastAPI (JVM baseline ~200MB vs ~50MB)
- Longer cold start (mitigated: Docker healthcheck delays traffic)
- More verbose than Python for simple CRUD

**Risks:**
- VPS 4GB RAM: JVM memory limit must be explicitly capped in Docker (800MB max)
  to leave room for PostgreSQL and Redis

## Notes

Spring Boot 3 requires Java 17 minimum; we use Java 21 for Virtual Threads.
Maven is used as build tool (Gradle is equally valid but Maven is more
common in academic/enterprise settings for documentation purposes).
