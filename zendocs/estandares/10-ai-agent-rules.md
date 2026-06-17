# AI Agent Rules — Kuris

Rules for AI-assisted development in this project. These apply to every
code generation session, regardless of the feature being built.

---

## Before Generating Any Code

### 1. Read the Spec First

If a spec file exists for the feature, read it entirely before writing a
single line of implementation. The spec is the contract.

```
docs/modules/{module}/specs/{FeatureName}.spec.md
```

If no spec exists, **do not generate implementation code**. Write the spec
first and get it approved.

---

### 2. Search for Existing Implementations

Before creating a new class, service, or pattern:

```bash
# Find existing service patterns
find src/main/java -name "*Service*.java" | head -20

# Find existing DTOs for the same module
find src/main/java -path "*/agenda/dto*" -name "*.java"

# Search for similar exception handling
grep -r "KurisException" src/main/java/
```

Reuse existing patterns. Do not create a second abstraction for something
that already exists.

---

### 3. Understand the Existing Architecture

Read these files before making any structural decision:

- `docs/architecture/01-high-level-architecture.md`
- `docs/standards/01-backend-standards.md` (layer rules)
- `docs/standards/03-database-standards.md` (migration rules)

---

## During Code Generation

### 4. Respect Layer Separation

| Layer | May call | May NOT call |
|---|---|---|
| Controller | Application service only | Repository, domain entities directly |
| Application service | Repository, domain entities, cache, notifications | Controller, HTTP objects |
| Domain entity | Other value objects | Any Spring bean |
| Repository | JPA only | Application service, business logic |

### 5. Multitenancy Is Non-Negotiable

Every repository method that reads or writes tenant data must include
`tenantId` as a parameter. No exceptions. Verify this on every generated query.

### 6. Document What You Generate

- Every new `public` class gets a class-level JavaDoc block
- Every new `public` method gets a method-level JavaDoc block
- If a decision was non-obvious, add a brief inline comment with `// WHY:`

### 7. Write the Test First

Follows TDD: generate the test class with failing test stubs before the
implementation class. Method names follow:
`methodName_givenCondition_expectedBehavior`

---

## Before Claiming a Task is Done

### 8. Verify Against Spec Test Cases

For each TC-XX in the spec, confirm there is a corresponding test that
asserts exactly the specified behavior.

### 9. Run the Full Test Suite Mentally

Before reporting completion, trace through:
- Does the happy path work end-to-end?
- Are all error cases from the spec handled?
- Does the tenant isolation test pass?
- Is coverage threshold maintained?

### 10. Verify Documentation Is Complete

- [ ] Spec status updated to `IMPLEMENTED`
- [ ] JavaDoc present on all public elements
- [ ] Module doc updated if the module's API or state machine changed
- [ ] ADR written if a significant architectural decision was made

---

## What NOT to Do

| Prohibited | Reason |
|---|---|
| Invent business rules not in the spec | Requirements come from use-cases.md and the spec |
| Add features "while you're at it" | Scope creep; creates untested code |
| Modify a committed Flyway migration | Would break other developers' databases |
| Use `git add .` | May commit .env files or generated artifacts |
| Bypass test failures with `--no-verify` | Tests exist for a reason |
| Create abstractions for hypothetical future needs | YAGNI — build for what exists now |
| Remove a security check for convenience | Always ask before touching security |
| Log passwords, tokens, or patient PII | Legal and security violation |

---

## Explaining Decisions

Every non-trivial architectural or implementation choice must be explained
in the response to the user, covering:

1. **What** was done
2. **Why** this approach (over alternatives)
3. **Trade-offs** accepted
4. **Risks** identified

This explanation feeds the ADR and the commit message body.
