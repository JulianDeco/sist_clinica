# ADR-005: Angular 18 as Frontend Framework

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: Foundation

---

## Context

ClinicaSaaS requires a frontend with: complex form management (appointment
booking with multi-step validation), role-based view control (doctors see
different views than secretaries), a calendar-based agenda view, and
integration with a Spring Boot REST API protected by JWT.

## Decision

Use **Angular 18** with **Angular Material** for the frontend.

Angular 18 provides: Signals for state management (no NgRx needed for MVP),
built-in HTTP interceptors for auth headers, RxJS for async operations,
and Angular Material for the full component library.

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| Next.js (App Router) | Previously used stack. The project is moving to Java/Spring — Angular aligns the tech stack more naturally for an enterprise/academic project |
| React (Vite SPA) | No built-in routing, state management, or HTTP interceptor conventions — requires assembling 4–5 separate libraries |
| Vue 3 | Smaller ecosystem for enterprise Angular Material–equivalent; fewer academic references |

## Consequences

**Positive:**
- Angular Material: full enterprise component library (calendar, tables, forms) without custom CSS
- Built-in dependency injection — consistent with Spring Boot patterns
- Interceptors: auth + tenant header injection in one place
- TypeScript strict mode: reduces runtime errors for complex data models
- Angular Signals (17+): no NgRx needed for MVP scale

**Negative / trade-offs:**
- Higher learning curve than React for simple components
- More boilerplate (modules/standalone components) than Next.js

**Risks:**
- Angular 18 Signals are relatively new — limited StackOverflow answers compared to NgRx patterns
