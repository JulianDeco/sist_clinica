# ADR-011: Spec-Driven Development as Mandatory Workflow

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation, testing standards, feature workflow

---

## Context

Kuris is an academic project with strict requirements for
documentation, traceability, and justification of implementation decisions
(Seminario de Trabajo Final UAI 2026). AI-assisted development introduces
the risk of generating code that does not match the intended requirements or
that introduces undocumented behaviors.

Additionally, with a single developer working across backend, frontend, and
infrastructure, there is a risk of implementing features from memory or
assumption rather than from explicit requirements.

## Decision

Adopt **Spec-Driven Development (SDD)** as a mandatory first step for every
feature implementation:

1. Write a spec file (`{Feature}.spec.md`) before any implementation code
2. Spec must be explicitly approved before coding starts
3. Spec defines: business rules (BR-XX), test cases (TC-XX), inputs, outputs
4. Tests are written from TC-XX items (TDD from spec)
5. Spec is committed alongside the implementation in the same PR

Spec files live in `docs/modules/{module}/specs/`.

## Consequences

**Positive:**
- Every feature has a written contract — defensible in thesis
- AI code generation is bounded by the spec (cannot add undocumented behavior)
- Test cases are derived from requirements, not from code (correct TDD direction)
- Traceability chain: use case → spec → test → implementation
- Open questions surfaced before implementation (not discovered mid-code)

**Negative / trade-offs:**
- Adds ~30 minutes per feature for spec writing
- Requires discipline: tempting to skip for "small" features

**Risks:**
- Spec becomes stale if not updated when implementation deviates:
  mitigated by requiring spec status update to IMPLEMENTED before PR merge

## Notes

This is inspired by RFC-first processes used at large tech companies
(Google Design Docs, Amazon PRFAQs) adapted to the scale of a one-developer
academic project.
