# ADR-007: Docker + Docker Compose for Deployment

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, infrastructure

---

## Context

The system runs on a single VPS with 4GB RAM. Development environment
must match production as closely as possible. The team is a single developer.

## Decision

Use **Docker** containers for all services and **Docker Compose** for
orchestration (dev and production). No Kubernetes for MVP.

Services: `nginx`, `backend` (Spring Boot), `frontend` (Angular built static),
`postgres`, `redis`.

## Consequences

**Positive:**
- Dev/prod parity: same images locally and on VPS
- Nginx, PostgreSQL, Redis require zero manual installation
- `docker compose up` starts the complete stack
- Memory limits per container enforceable via `mem_limit`

**Negative / trade-offs:**
- No auto-scaling (single VPS, not needed for MVP 1–5 clinics)
- No rolling deploys (brief downtime on `docker compose up` with new image)

**Risks:**
- VPS 4GB RAM: total container memory must stay under 1.5GB to leave OS overhead
  See: [High-Level Architecture §7](../architecture/01-high-level-architecture.md) for limits
