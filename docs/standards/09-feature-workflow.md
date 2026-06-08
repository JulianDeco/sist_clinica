# Feature Development Workflow

> Applies to: every feature, bug fix, refactor, and enhancement.
> Methodology: SDD → TDD → Clean Architecture → DDD → ADR.
> **Never skip steps. Never guess business rules. Stop and ask when ambiguous.**

---

## Mandatory Development Order

```
1. Specification        ← no code before this is APPROVED
2. ADR impact analysis  ← architectural decisions before domain design
3. Domain design        ← DDD: entities, value objects, aggregates, events
4. Test design          ← derive test cases from spec acceptance criteria
5. Test implementation  ← write failing tests (Red)
6. Production code      ← implement minimum to pass tests (Green → Refactor)
7. Documentation        ← spec IMPLEMENTED, JavaDoc, module doc, ADR
8. Review               ← PR against develop, checklist, approval
```

---

## Definition of Ready

A task cannot start implementation until ALL of these are true:

- [ ] Specification written and **explicitly approved**
- [ ] Acceptance criteria defined (AC-XX in spec)
- [ ] Edge cases identified
- [ ] Dependencies known (other modules, FHIR resources, DB changes)
- [ ] ADR impact evaluated (does this decision need a new ADR?)

---

## Definition of Done

A task is complete only when ALL of these are true:

- [ ] Specification status updated to `IMPLEMENTED`
- [ ] All tests implemented and passing (`mvn verify` / `ng test`)
- [ ] Coverage threshold maintained (80% backend / 75% frontend)
- [ ] All documentation updated (JavaDoc, module doc, ADR if needed)
- [ ] Architecture respected — no layer violations
- [ ] No critical code smells
- [ ] PR reviewed and approved
- [ ] `tasks.json` → `estado: hecho` + `fecha_fin`

---

## Step 1 — Specification (SDD)

**Goal**: Produce the written contract before any code is written.

**Location**: `docs/specifications/{FeatureName}.spec.md`

Every spec must include:

| Section | Content |
|---|---|
| Business goal | Why this feature exists |
| Functional requirements | What the system must do (FR-XX) |
| Non-functional requirements | Performance, security, constraints (NFR-XX) |
| Acceptance criteria | Verifiable conditions for done (AC-XX) |
| Edge cases | All non-happy-path scenarios |
| Constraints | Technical or business limits |
| Dependencies | Other modules, FHIR resources, external services |
| Risks | What could go wrong |
| Open questions | Unresolved decisions — **resolve before implementation** |

**Blocker**: no implementation starts before spec status is `APPROVED`.
If requirements are ambiguous → stop and ask. Never guess business rules.

Spec template: `docs/specifications/_template.spec.md`

---

## Step 2 — ADR Impact Analysis

**Goal**: Confirm existing architecture handles this feature, or decide and
document how it must evolve.

Actions:
- Read `docs/architecture/01-high-level-architecture.md`
- Identify which layers are touched (Domain / Application / Infrastructure / Presentation)
- Search for existing patterns in `src/` — reuse before creating
- Ask: does this require a new architectural decision?
  - If **yes** → write ADR in `docs/adr/` **before** domain design
  - If **no** → document confirmation in spec (reference existing ADRs)
- Validate DB impact: migration needed?
- Validate cache impact: Redis keys to invalidate?

**Rule**: never generate code that bypasses the defined architecture.
Prefer simple solutions. Prefer composition over inheritance.
Avoid premature abstractions and overengineering.

---

## Step 3 — Domain Design (DDD)

**Goal**: Model the domain before touching infrastructure or frameworks.

Actions:
- Identify **Entities** (have identity, mutable state)
- Identify **Value Objects** (defined by value, immutable)
- Identify **Aggregates** (consistency boundary) and their root
- Identify **Domain Events** (something that happened)
- Identify **Domain Services** (logic that doesn't belong to one entity)
- Map state machines for entities with lifecycle (e.g. Appointment status)

**Rule**: Domain layer must not depend on any framework (no Spring annotations
on domain objects except JPA mapping annotations).
Business rules belong in Domain and Application layers only.

Deliverable: class diagram (Mermaid) added to spec or module doc.

---

## Step 4 — Test Design

**Goal**: Derive test cases directly from the spec acceptance criteria.

Actions:
- For each AC-XX in the spec, write at least one TC-XX (test case)
- Classify each test: unit (use case / domain) or integration (controller / repository)
- For integration tests: identify Testcontainers requirements
- Define tenant isolation test (mandatory for every new repository method)
- Document TC-XX list in the spec file under `## Test Cases`

Deliverable: TC-XX list in spec; test class stubs created (empty test methods).

---

## Step 5 — Test Implementation (TDD — Red phase)

**Goal**: Write failing tests before any production code.

```
Red → Green → Refactor
```

Order:
1. Write unit tests for domain entities (state transitions, invariants)
2. Write unit tests for application service / use case
3. Write integration tests for controller (full HTTP round-trip)
4. Write repository integration test with tenant isolation assertion
5. Run all tests → confirm they **fail** (Red)

**Rule**: never write production code before the failing test exists.
Test names follow: `method_givenCondition_expectedBehavior`

---

## Step 6 — Production Implementation (TDD — Green → Refactor)

**Goal**: Write minimum code to make tests pass, then refactor.

Backend order (inside → outside — Clean Architecture):
1. Domain entity / value object
2. Repository interface (in `domain/`)
3. Application service / use case (`application/`)
4. Infrastructure adapter — JPA repository impl (`infrastructure/persistence/`)
5. Redis adapter if caching needed (`infrastructure/cache/`)
6. Controller + DTOs (`api/v1/`)
7. Exception classes if new error cases introduced
8. Flyway migration (if DB changes — commit separately)

Frontend order:
1. TypeScript model (`features/{x}/models/`)
2. API client method (`core/api/`)
3. Store update (Signals)
4. Container component
5. Presentational components
6. Route wiring

**After each class**: run `mvn test` (backend) or `ng test` (frontend).
**JavaDoc**: write it at creation time, not after.

---

## Step 7 — Documentation

**Goal**: Leave every artifact in a state ready for thesis review and future
sessions.

Actions:
- Update spec status → `IMPLEMENTED`
- Update `docs/modules/{module}.md` if state machine or API changed
- Write/update JavaDoc on all new public classes and methods
- Write ADR if a significant architectural decision was made during implementation
- Append to `docs/adr/decisions-log.md` for informal decisions
- Update `tasks.json` → `estado: testeando`

---

## Step 8 — Review + Merge

**Goal**: Validate against Definition of Done before merging.

Actions:
- Open PR against `develop` using the PR template (`docs/standards/08-git-standards.md`)
- Self-review against DoD checklist above
- Request review from @julian
- Address all review comments
- Merge only with approval
- Update `tasks.json` → `estado: hecho` + `fecha_fin`

---

## Traceability Matrix

Every feature must maintain this chain end-to-end:

```
Use Case (use-cases.md)
  └── Task (tasks.json  ← single source of truth)
        └── Branch (feature/T-XXX-name)
              └── Spec (docs/specifications/{Feature}.spec.md)
                    └── ADR (docs/adr/ADR-NNN-*.md) if applicable
                          └── Test cases (TC-XX in spec)
                                └── Tests (named after TC-XX)
                                      └── Implementation (domain → application → infra → api)
                                            └── PR (links task + spec + ADR)
```

This chain allows the thesis committee, a reviewer, or a future session
to follow any business requirement all the way to the code implementing it.

---

## Hard Rules

| Rule | Consequence of breaking |
|---|---|
| No spec → no code | Blocked — write spec first |
| Ambiguous requirement | Stop and ask — never guess |
| Test before production code | Blocked — write failing test first |
| Layer violation | Rejected in code review |
| Missing tenant_id in query | Security issue — mandatory fix before merge |
| Missing JavaDoc on public API | Rejected in code review |
| `git add .` | Blocked — add specific files only |
| Modify committed Flyway migration | Blocked — add new migration instead |
