# Decisions Log (Informal, Append-Only)

This file captures non-obvious decisions as they happen, before they are
formalized into ADRs. Append only — never edit past entries.

---

## 2026-06-08 — Full stack migration from Python/FastAPI to Java 21 + Spring Boot

**Context**: The project had a committed Python/FastAPI + Next.js stack with
auth code and migrations. Decision made to migrate to a Java enterprise stack.

**Decision**: Discard FastAPI + Next.js entirely. Adopt Java 21 + Spring Boot 3
+ Angular 18. All context files, CLAUDE.md, and documentation rewritten.

**Reason**: Spring Boot provides more mature RBAC, exception handling, and
testing infrastructure for the complexity of FHIR R4 + multitenant use cases.
Angular 18 aligns better with an enterprise/academic project than Next.js.

**Formalized in**: ADR-001 (Spring Boot), ADR-005 (Angular)

---

## 2026-06-08 — Adopted Spec-Driven Development as mandatory workflow

**Context**: AI-assisted development can generate code that doesn't match
requirements. Single-developer project needs discipline to avoid assumption-driven
implementations. Thesis requires traceable documentation.

**Decision**: Every feature must start with a spec file
(`docs/modules/{x}/specs/{Feature}.spec.md`) approved before coding begins.
Tests written from spec test cases (TDD from spec).

**Reason**: Traceability (UC → spec → test → code) is academically valuable
and prevents undocumented behaviors in AI-generated code.

**Formalized in**: ADR-011

---

## 2026-06-08 — Row-level multitenancy confirmed for Java stack

**Context**: VPS 4GB RAM limits. One PostgreSQL database shared by all tenants.

**Decision**: Same decision as before (carried from Python stack): row-level
with `tenant_id` column. Enforcement via `TenantContextFilter` ThreadLocal
and `TenantAwareRepository` base class in Spring Data JPA.

**Formalized in**: ADR-003

---

## 2026-06-09 — Separar identidad de membresía para usuarios multi-tenant

**Context**: El schema original tenía `users.tenant_id` hardcodeado, lo que
impedía que una persona trabajara en múltiples clínicas con una sola cuenta.
Caso real: médico con dos consultorios, secretaria compartida.

**Decision**: Separar identidad (tabla `users`, email único global) de membresía
(tabla `user_tenants`: user × tenant × role). El login devuelve un identity token
de 5 minutos; el session token con `tenant_id` + `role` se emite al seleccionar
tenant. Un botón "Cambiar clínica" en el navbar permite el switch sin re-login.

**Reason**: UX real para usuarios compartidos. El flujo de dos pasos agrega un
solo round-trip que se omite automáticamente si el usuario pertenece a un solo tenant.

**Formalized in**: ADR-014
