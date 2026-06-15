# ADR-004: Redis for Cache and Ephemeral State Only

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation

---

## Context

Several system operations are hot paths that would be too slow if they
hit PostgreSQL on every request: RBAC permission checks (every API call),
insurance coverage limit lookups (every booking), and JWT revocation checks.

## Decision

Use **Redis 8** for caching and ephemeral state only. Redis is never the
system of record — PostgreSQL is authoritative. If Redis is unavailable,
the system falls back to the database and continues operating.

Allowed uses:
1. RBAC permission cache per user/tenant (TTL 5 min)
2. Insurance coverage weekly counter (TTL 1 hour)
3. No-show risk score cache (TTL 30 min)
4. JWT JTI revocation list (TTL = token remaining TTL)
5. Notification deduplication keys (TTL 48 hours)

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| Caffeine (in-memory cache) | Not shareable across future instances; loses data on restart |
| Hazelcast | Too heavy for VPS 4GB RAM; operational complexity |
| No cache | RBAC permission DB query on every API call would add 5–20ms latency per request |

## Consequences

**Positive:**
- Sub-millisecond permission lookups
- Coverage counter updates atomic (Redis DECR is atomic)
- JWT revocation without storing all tokens in DB

**Negative / trade-offs:**
- Extra infrastructure component
- Cache invalidation logic must be maintained
- Stale cache window: up to 5 min for permissions (acceptable for MVP)

**Risks:**
- Redis OOM: `maxmemory-policy allkeys-lru` + 128MB limit set in Docker
  prevents the container from growing unbounded
