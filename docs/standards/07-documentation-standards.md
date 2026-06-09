# Documentation Standards

> Principle: every decision, every class, every public method must be
> documented at the time of creation — not retroactively. Documentation
> is a first-class deliverable, not an afterthought.

---

## 1. Documentation Levels

| Level | What | Where |
|---|---|---|
| **Architecture** | Why a technology or pattern was chosen | `docs/adr/` (ADRs) |
| **Feature spec** | What a feature must do before coding begins | `docs/modules/specs/` |
| **Module context** | How a module works, its contracts and state machine | `docs/modules/` |
| **Code-level** | What a class or method does | JavaDoc on every public element |
| **Decision log** | Why code was written a specific way (non-obvious) | Inline comment when critical |

---

## 2. JavaDoc Standards

Every `public` class, interface, and method in the backend requires JavaDoc.

### Class-Level JavaDoc

```java
/**
 * Application service implementing the book-appointment use case (UC-01).
 *
 * <p>Orchestrates slot availability check, insurance coverage validation,
 * no-show risk scoring, and atomic appointment creation.
 *
 * <p>Business rules enforced:
 * <ul>
 *   <li>The requested slot must be in {@code FREE} status.</li>
 *   <li>Patient coverage weekly limit must not be exceeded.</li>
 *   <li>If risk score exceeds threshold and practitioner has overbooking
 *       enabled, an overbooking suggestion is included in the result.</li>
 * </ul>
 *
 * <p>See spec: {@code docs/modules/agenda/specs/BookAppointment.spec.md}
 *
 * @see BookAppointmentCommand
 * @see AppointmentResult
 */
@Service
@Transactional
public class BookAppointmentUseCaseImpl implements BookAppointmentUseCase { ... }
```

### Method-Level JavaDoc

```java
/**
 * Executes the book-appointment use case.
 *
 * <p>Performs slot reservation, coverage decrement, and risk score
 * attachment in a single database transaction. On failure, the
 * transaction is rolled back and no side effects are committed.
 *
 * @param command the booking request containing slot, patient, and tenant identifiers
 * @return the created appointment with its FHIR ID and risk assessment
 * @throws SlotAlreadyBookedException  if the slot is not in FREE status
 * @throws CoverageExceededException   if the patient's weekly coverage limit is reached
 * @throws ResourceNotFoundException   if slot or patient does not exist in this tenant
 */
AppointmentResult execute(BookAppointmentCommand command);
```

### Rules

- Do not repeat what the method name already says — document the **why** and **constraints**
- Always document `@throws` for checked and meaningful runtime exceptions
- Reference the spec file and related ADRs with `@see` or inline link
- Avoid Javadoc on private methods unless the logic is non-obvious

---

## 3. Spec File Documentation (Spec-Driven Development)

Every feature starts with a spec file before any implementation.
See [Testing Standards §5](../testing/01-testing-standards.md) for the full spec template.

Key rule: the spec file is **committed before the first line of implementation code**.

---

## 4. Module Documentation Files

For every backend module, create `docs/modules/{module}.md`:

```markdown
# Module: Agenda

**Status**: IN DEVELOPMENT
**Last updated**: 2026-06-08
**Relates to**: UC-01, UC-04

---

## Responsibility
Manages appointment booking, slot management, and overbooking logic.

## Domain Model
(Mermaid class diagram)

## State Machine
(Appointment status transitions)

## Public API
(Endpoint list)

## Business Rules
(Numbered list)

## Dependencies
(Other modules this module calls)

## Configuration
(Configurable parameters)

## Known Limitations / TODOs
```

---

## 5. ADR Format

See `docs/adr/README.md` for the full ADR template. Every ADR is immutable
once merged — amend by creating a new ADR that supersedes it.

---

## 6. Code Comment Policy

Inline comments are rare and high-value only:

```java
// CORRECT — explains a non-obvious constraint
// BCrypt strength 12 is deliberately higher than default (10) to increase
// brute-force cost. At 300ms/hash this is acceptable for login latency.
return new BCryptPasswordEncoder(12);

// WRONG — restates what the code already says
// Create a new BCrypt encoder with strength 12
return new BCryptPasswordEncoder(12);
```

Rules:
- Comment the **why**, never the **what**
- Use TODO comments for known limitations: `// TODO(T-042): add rate limiting`
- Never leave commented-out code — delete it, Git history is the archive

---

## 7. Commit Message as Documentation

Commit messages are permanent project documentation. Follow Conventional Commits:

```
feat(agenda): implement slot availability check for UC-01

Validates that a requested slot is in FREE status before booking.
Returns SlotAlreadyBookedException (409) if occupied.

Spec: docs/modules/agenda/specs/BookAppointment.spec.md
ADR: docs/adr/ADR-009-appointment-booking-rules.md
```

- Subject line: `{type}({scope}): {imperative, lowercase, no period}`
- Body: explain *why* the change was made if not obvious from the title
- Reference spec and ADR when relevant

---

## 8. Documentation Workflow

```
New feature:
  1. Write spec file (docs/modules/{x}/specs/{X}.spec.md)
  2. Get spec approved (PR review or explicit OK in chat)
  3. Write implementation + tests
  4. Write/update module doc (docs/modules/{x}.md)
  5. Write ADR if a significant decision was made
  6. Commit everything together (spec + code + tests + module doc)

All in one PR — documentation is never a follow-up.
```

---

## 9. Decision Log

Every non-obvious technical decision made during a session is recorded in
a decision log entry at `docs/adr/decisions-log.md` (informal, append-only)
before a formal ADR is written. This captures the reasoning while it is fresh.

Format:
```markdown
## 2026-06-08 — Chose row-level multitenancy over schema-per-tenant

**Context**: VPS has 4GB RAM. Schema-per-tenant would require N connection pools.
**Decision**: Row-level with tenant_id column.
**Trade-off**: All tenants share tables — a missing WHERE tenant_id clause
  would leak data cross-tenant. Mitigated by TenantAwareRepository base class.
**Formalized in**: ADR-003
```
