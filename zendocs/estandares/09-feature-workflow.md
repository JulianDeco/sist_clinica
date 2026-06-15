# Feature Development Workflow

> Applies to: every feature, bug fix, refactor, and enhancement.
> Methodology: SDD → TDD → Clean Architecture → DDD → ADR.
> **Never skip steps. Never guess business rules. Stop and ask when ambiguous.**

---

## TDD Cycle — Explicit Rules

Every unit of implementation (domain entity, use case, controller endpoint,
Angular component) follows this cycle without exception:

```
RED   → Write a failing test that expresses the requirement.
         Run mvn test / ng test → confirm it FAILS.
         If it passes immediately, the test is wrong or redundant.

GREEN → Write the minimum production code to make the test pass.
         No extra logic, no "while I'm here" cleanup.
         Run mvn test / ng test → confirm it PASSES.

REFACTOR → Clean up without changing behavior.
            Rename, extract, simplify. Run tests again → still GREEN.
            Only then commit.
```

**Hard blockers for TDD:**

| Situation | Required action |
|---|---|
| Writing production code with no failing test | STOP — write the test first |
| Test passes on first run before any impl | DELETE it — it tests nothing |
| Skipping Red phase ("I know it will fail") | NOT acceptable — run it, see it fail |
| Committing Green code before Refactor | Only allowed if refactor is a separate commit |
| Mocking the database in integration tests | FORBIDDEN — use Testcontainers |

**Scope of TDD per layer (backend):**

| Layer | Test type | Tooling |
|---|---|---|
| Domain entity / value object | Unit | JUnit 5 + AssertJ |
| Application service / use case | Unit | JUnit 5 + Mockito |
| Controller (HTTP round-trip) | Integration | `@SpringBootTest` + MockMvc + Testcontainers |
| Repository (SQL + tenant isolation) | Integration | `@DataJpaTest` + Testcontainers |

**Scope of TDD per layer (frontend):**

| Layer | Test type | Tooling |
|---|---|---|
| Store (Signals state) | Unit | Jasmine + Karma |
| API client service | Unit | `HttpClientTestingModule` |
| Guard / interceptor | Unit | TestBed |
| Container component | Component | Angular Testing Library |

---

## Mandatory Development Order

```
1. Specification        ← no code before this is APPROVED
2. ADR impact analysis  ← architectural decisions before domain design
3. Domain design        ← DDD: entities, value objects, aggregates, events
4. Test design          ← TC-XX derived from AC-XX in spec; test stubs created
5. TDD — Red phase      ← write failing tests; mvn test / ng test confirms FAIL
6. TDD — Green phase    ← minimum production code to pass tests; confirm PASS
7. TDD — Refactor       ← clean up; tests still GREEN; commit
8. Documentation        ← spec IMPLEMENTED, JavaDoc, module doc, ADR
9. Review               ← PR against develop, checklist, approval
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

Deliverable: TC-XX list in spec; test class stubs created (empty `@Test` methods
annotated with `@Disabled("TDD: not yet implemented")` so the build stays green
until Step 5 removes the annotation and implements the assertion).

---

## Step 5 — TDD Red Phase (failing tests first)

**Goal**: Every production class is preceded by a failing test. No exceptions.

Order:
1. Create test class for domain entity — write assertions for state transitions
   and invariants. The domain class does **not exist yet** — the test file will
   not compile. That is expected and correct.
2. Create test class for application use case — mock all collaborators with
   Mockito. The use case class does **not exist yet**.
3. Create integration test class for controller — set up MockMvc, Testcontainers.
4. Create repository integration test — include tenant isolation assertion.
5. Run `mvn test` (backend) or `ng test` (frontend).
   **Required outcome**: compilation errors or test failures. If everything
   passes, the test is wrong — fix it before proceeding.

**Rule**: the Red phase ends only when `mvn test` shows failing tests (not
passing, not compilation warnings — actual failures or errors).

Test names follow: `method_givenCondition_expectedBehavior`

---

## Step 6 — TDD Green Phase (minimum code to pass)

**Goal**: Make the failing tests pass with the least code necessary.

Order (backend — inside → outside, Clean Architecture):
1. Domain entity / value object → makes domain unit tests compile and pass
2. Repository interface (in `domain/`) → needed by use case
3. Application use case → makes use case unit tests pass
4. Infrastructure JPA repository impl → makes repository integration tests pass
5. Redis adapter if caching needed
6. Controller + DTOs → makes controller integration tests pass
7. Exception classes for new error cases

Run `mvn test` after each class. Stay in Green phase until all tests pass.

**Rule**: never add logic beyond what the failing test demands. If no test
requires it, it does not get implemented.

---

## Step 6b — TDD Refactor Phase

**Goal**: Improve structure without changing behavior.

Actions:
- Rename for clarity, extract private methods, eliminate duplication
- Run `mvn test` after every change → must stay GREEN
- If a refactor breaks a test, revert — the test is correct, the refactor is wrong

Deliverable: clean code with all tests still passing. Commit here.

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
