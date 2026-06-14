# Módulo: Frontend Angular 18

> Contexto compacto del módulo. El estándar completo (TypeScript, Signals,
> componentes, testing) está en `docs/standards/02-frontend-standards.md` —
> leerlo antes de codificar. Auth: spec `docs/specifications/T-003-auth-jwt.spec.md`
> + `docs/adr/ADR-014-multi-tenant-membership.md`.

## Stack

- Angular 18 — standalone components (sin NgModules), `inject()` en lugar de constructor DI
- Angular Material — tema azul médico: `mat.$azure-palette` (primary) +
  `mat.$cyan-palette` (tertiary) definido en `src/styles.scss`
- Estado con **Signals** (`signal`/`computed`) — **NO NgRx** ni otra librería de estado
- Package manager: **pnpm**

## Estructura real (scaffold T-002)

```
src/frontend/src/app/
├── app.config.ts / app.routes.ts
├── core/
│   ├── api/auth.api.ts              # cliente HTTP /api/v1/auth/*
│   ├── auth/
│   │   ├── auth.service.ts          # máquina de estados 3 fases (ADR-014)
│   │   ├── auth.guard.ts            # protege /app/* (requiere READY)
│   │   ├── role.guard.ts            # por rol del tenant activo
│   │   ├── select-tenant.guard.ts   # requiere IDENTITY_CONFIRMED
│   │   └── models/                  # user, token, tenant-membership
│   ├── interceptors/                # auth (Bearer), tenant, error (401 → limpiar estado)
│   └── tenant/tenant.service.ts
├── shared/                          # componentes/pipes/directivas reutilizables (vacío aún)
├── features/                        # lazy-loaded vía *.routes.ts
│   ├── landing/ login/ select-tenant/ unauthorized/    # implementados
│   └── agenda/ patients/ clinical/ reports/ settings/  # placeholders (T-012..T-014)
└── layout/
    ├── main-layout/                 # shell con router-outlet
    ├── navbar/                      # incluye tenant switcher
    └── sidebar/
```

Rutas: `/` (landing) → `/login` → `/select-tenant` (selectTenantGuard) →
`/app/*` (authGuard + MainLayout; hijos lazy con `loadChildren`).

## AuthService — máquina de estados 3 fases (ADR-014)

```
UNAUTHENTICATED ──login()──▶ IDENTITY_CONFIRMED ──selectTenant()──▶ READY
   (sin tokens)              (identityToken 5min,                  (accessToken 30min,
                              lista de tenants)                     tenant activo)
```

- Si el usuario tiene **un solo tenant**, `login()` encadena `selectTenant()`
  automáticamente (salta la pantalla de selección).
- `switchTenant()` cambia de clínica activa sin re-login (nuevo accessToken).
- Estado expuesto como signals: `authState`, `currentUser`, `availableTenants`,
  `activeTenant` + `isAuthenticated` (computed).
- **Tokens solo en memoria** — nunca localStorage/sessionStorage (NFR-03).
  El refresh token vive en cookie httpOnly que maneja el backend (T-003).

## Convenciones

- Archivos `kebab-case.component.ts` / clases `PascalCase`
- Máximo **150 líneas por componente** — extraer subcomponentes si supera
- Antes de crear un componente: buscar en `shared/` y `layout/`
- Un `*.routes.ts` por feature; siempre lazy desde `app.routes.ts`

## NO HACER

- No guardar tokens en localStorage/sessionStorage — solo memoria (XSS)
- No verificar firma JWT en el frontend — solo decodificar para UX
- No usar guards/roles del frontend como barrera de seguridad real — solo UX
- No introducir NgRx/RxJS-store — Signals + servicios singleton
- No llamar a la API en cada keystroke — debounce

## Dependencias

→ `docs/standards/02-frontend-standards.md` (estándar completo)
→ `docs/specifications/T-003-auth-jwt.spec.md` (contrato auth backend)
→ `docs/adr/ADR-014-multi-tenant-membership.md` (flujo two-step)
→ `.claude/context/modules/02-rbac.md` (roles y permisos)
