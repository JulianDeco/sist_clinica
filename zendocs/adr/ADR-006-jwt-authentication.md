# ADR-006: JWT Stateless Authentication with Refresh Token Rotation

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, security

---

## Context

Kuris is a stateless REST API. Authentication must be: scalable
(no server-side session store), secure (protect against token theft),
and practical (no re-login every 30 minutes).

## Decision

Use **stateless JWT access tokens** (30-minute TTL) combined with
**single-use refresh token rotation** (7-day TTL, stored hashed in PostgreSQL,
delivered via httpOnly cookie).

JWT claims include: `sub` (userId), `tenant_id`, `role`, `jti` (for revocation).
Permissions are NOT embedded in the JWT — they are loaded from Redis cache
on demand to avoid stale authorization data.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| Server-side sessions | Requires session store; less scalable; not idiomatic for REST |
| Long-lived JWT (7 days, no refresh) | Cannot revoke on logout or compromise |
| OAuth2 Authorization Code + PKCE | Correct for 3rd-party delegation; not needed for same-origin SPA + API |

## Consequences

**Positive:**
- Stateless — no session DB required
- Short-lived access tokens limit exposure window to 30 minutes
- Refresh token rotation: automatic re-issue + old token revocation prevents silent theft
- httpOnly cookie for refresh token: not accessible by JavaScript (XSS protection)

**Negative / trade-offs:**
- Access token cannot be revoked mid-lifetime (30 min) without a JTI blocklist in Redis
  — JTI blocklist added for logout use case
- Refresh token rotation requires PostgreSQL write on every refresh (acceptable)

**Risks:**
- Stolen refresh token before rotation: mitigated by detecting reuse (revoke entire token family)
