# ADR-014: Multi-Tenant User Membership — Identity Separated from Tenancy

**Status**: ACCEPTED
**Date**: 2026-06-09
**Author**: Julián Deco
**Supersedes**: none — extends ADR-003 (row-level multitenancy) and ADR-006 (JWT auth)
**Relates to**: T-002, T-003, T-004

---

## Context

The original schema (`V003__create_users.sql`) modeled `users` as a
tenant-scoped entity: each row has a `tenant_id` FK, and `email` has a
global UNIQUE constraint. This works for a single-tenant user but breaks
the real-world case where **one person works at multiple clinics** (e.g., a
doctor with two practices, a shared secretary). Under the old model such a
person would need two accounts with two emails, which is poor UX and
duplicates identity data.

ADR-006 embeds `tenant_id` and `role` directly in the JWT access token,
which assumed a 1:1 user-to-tenant relationship. We need to revise both
the data model and the auth flow.

---

## Decision

**Separate identity from membership.**

### Data model

```
users (global identity)
  id, email, password_hash, full_name, active, ...

user_tenants (membership — one row per user×tenant)
  user_id  → users.id
  tenant_id → tenants.id
  role_id   → roles.id
  active    BOOLEAN
  joined_at TIMESTAMPTZ
  PRIMARY KEY (user_id, tenant_id)
```

`users.email` remains globally unique — one account per real person.
`users` has no `tenant_id` column.
`user_roles` (old pivot) is replaced by `user_tenants.role_id` — a user
has exactly one role per tenant (simplest model for MVP; extend later if needed).

### Auth flow (two-step)

```
Step 1 — Identity token
  POST /api/v1/auth/login { email, password }
  → 200 { identityToken, tenants: [{ tenantId, tenantName, role }] }
       identityToken: short-lived JWT (5 min), claims: { sub, purpose:"tenant-select" }
  → If tenants.length === 1: proceed directly as if Step 2 was called.

Step 2 — Session token
  POST /api/v1/auth/select-tenant { tenantId }
  Authorization: Bearer <identityToken>
  → 200 { accessToken }  +  Set-Cookie: refreshToken (httpOnly, 7d)
       accessToken: JWT 30 min, claims: { sub, tenant_id, role, jti }

Switch tenant (authenticated user)
  POST /api/v1/auth/switch-tenant { tenantId }
  Authorization: Bearer <accessToken>
  → 200 { accessToken }  +  rotated refreshToken cookie
       Previous access token revoked via JTI blocklist in Redis.
```

### Frontend state machine (AuthService)

```
UNAUTHENTICATED
    → login() success → IDENTITY_CONFIRMED  (identityToken in memory, tenants list)
IDENTITY_CONFIRMED
    → selectTenant()  → READY               (accessToken in memory, tenantId set)
    → auto-select if single tenant
READY
    → switchTenant()  → READY               (new tenant context)
    → logout()        → UNAUTHENTICATED
```

`AuthGuard` requires state === READY.
`/select-tenant` route is accessible only in IDENTITY_CONFIRMED state.

---

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| Keep `tenant_id` in `users`, allow duplicate emails per tenant | Breaks global UNIQUE on email; UX nightmare for shared users |
| OAuth2 with per-tenant IdP | Correct for enterprise SSO; massive overkill for 1–5 person clinics |
| Separate `user_roles` per tenant (no `user_tenants` pivot) | More joins, same semantics — `user_tenants` is cleaner |
| Embed tenant list in access token | Token bloat; list changes when user joins/leaves a tenant mid-session |

---

## Consequences

**Positive:**
- One account, multiple clinics — real-world UX for shared practitioners.
- `users` is now a true global identity; all tenant data is in `user_tenants`.
- Switching clinics does not require re-entering credentials.
- Clean separation: identity token (prove who you are) vs session token (prove where you work).

**Negative / trade-offs:**
- Two-step login adds one HTTP round-trip for multi-tenant users (negligible; single-tenant users skip it automatically).
- `V003` must be replaced by `V010` migration that drops `tenant_id` from `users` and creates `user_tenants` — breaking change on the existing schema (acceptable at scaffold stage, no prod data yet).
- `user_roles` pivot is dropped in favor of `user_tenants.role_id` — assumes one role per tenant per user (MVP constraint; revisit post-MVP if a doctor can be both DOCTOR and ADMIN in the same clinic).

**Risks:**
- Identity token window (5 min) is tight for slow connections → mitigated by auto-retry on `/select-tenant` 401 with re-login prompt.
- Switch-tenant invalidates current access token → any in-flight requests during the switch get a 401 → `ErrorInterceptor` must not redirect to login on 401 during switch (use a `isSwitchingTenant` flag, same pattern as `isRedirecting`).
