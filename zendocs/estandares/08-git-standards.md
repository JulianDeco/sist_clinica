# Git Standards — Branch Strategy + Conventional Commits

---

## 1. Branch Strategy (Git Flow)

```
main          ← production; only merges from release/* or hotfix/*
develop       ← integration; base for all feature branches
feature/*     ← one task per branch; branched from develop
release/*     ← release preparation (version bump, changelog, final tests)
hotfix/*      ← urgent production fix; branches from main, merges to main + develop
```

### Branch Naming

```
feature/T-{taskId}-{short-description}     feature/T-012-book-appointment-endpoint
hotfix/{issue-description}                 hotfix/jwt-expiry-bug
release/v{major}.{minor}.{patch}           release/v0.1.0
```

---

## 2. Commit Convention (Conventional Commits)

```
{type}({scope}): {imperative description, lowercase, no period}

{optional body: why this change was made}

{optional footer: references}
```

### Types

| Type | When |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `test` | Add or fix tests |
| `docs` | Documentation only |
| `refactor` | Code change with no behavior change |
| `chore` | Build, deps, config, CI |
| `spec` | New or updated spec file (SDD) |
| `adr` | New Architecture Decision Record |

### Scopes

`auth` · `rbac` · `agenda` · `patients` · `clinical` · `intelligence`
`coverage` · `notifications` · `fhir` · `frontend` · `db` · `infra` · `ci`

### Examples

```
feat(agenda): implement slot availability validation for UC-01
test(agenda): add integration tests for book-appointment controller
fix(auth): prevent refresh token reuse after rotation
spec(agenda): add BookAppointment spec file
adr: add ADR-009 documenting appointment booking rules
docs(rbac): update module doc with permission invalidation flow
chore(infra): add memory limits to docker-compose services
refactor(fhir): extract FhirResourceMapper to dedicated class
```

---

## 3. Workflow per Task

```
1.  git checkout develop && git pull
2.  git checkout -b feature/T-XXX-short-name
3.  Write spec file (if new feature)  ← MANDATORY before code
4.  Write failing tests (TDD)
5.  Implement until tests pass
6.  Update module documentation
7.  git add {specific files}          ← NEVER git add .
8.  git commit -m "..."
9.  git push origin feature/T-XXX-short-name
10. Open PR → develop
11. Request review
12. Merge only with approval
```

**Commit frequency**: at minimum one commit per logical step (test written,
feature implemented, docs updated). Do not batch an entire feature into one commit.

---

## 4. Pull Request Template

```markdown
## Summary
<!-- What does this PR do? One paragraph. -->

## Related Task
<!-- T-XXX link -->

## Spec
<!-- Link to spec file, or N/A -->

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactor
- [ ] Documentation
- [ ] Infrastructure / configuration

## Testing
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] Coverage threshold maintained (80% backend / 75% frontend)

## Documentation
- [ ] Spec file written (if new feature)
- [ ] Module doc updated
- [ ] ADR created (if significant architectural decision)
- [ ] JavaDoc on all new public classes and methods

## Checklist
- [ ] Tests pass locally
- [ ] No secrets or .env files committed
- [ ] No file exceeds 200 lines without justification
- [ ] git add with specific files (not git add .)
- [ ] Commit messages follow Conventional Commits
- [ ] PR targets develop (not main)
```

---

## 5. Code Review Checklist

Reviewer responsibilities before approving:

**Correctness**
- [ ] Logic matches the spec business rules
- [ ] Edge cases and error paths handled
- [ ] No race conditions in concurrent paths

**Architecture**
- [ ] Correct layer separation (no business logic in controllers)
- [ ] tenant_id present in all repository queries
- [ ] No new abstractions without justification

**Security**
- [ ] No secrets hardcoded or logged
- [ ] Input validated at HTTP boundary
- [ ] Auth/permission check present on new endpoints

**Tests**
- [ ] Tests cover all spec test cases
- [ ] Integration test includes tenant isolation check
- [ ] Test names describe behavior (not implementation)

**Documentation**
- [ ] JavaDoc on all public classes and methods
- [ ] Spec file present (if new feature)
- [ ] Module doc updated (if module changed)

---

## 6. Rules

- Never commit directly to `main` or `develop`
- Never force-push to `develop` or `main`
- PRs to `main` require approval from @julian
- Every PR has a linked task (T-XXX)
- Delete feature branches after merge
- No `--no-verify` to skip hooks — fix the hook failure instead
