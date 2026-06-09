# Specification: Scaffold Frontend — Angular 18 + Angular Material

**Status**: APPROVED
**Author**: Julian Deco
**Date**: 2026-06-09
**Task**: T-002 (`tasks.json`)
**Branch**: `feature/T-002-scaffold-frontend`
**Relates to**: transversal (base para T-011..T-014)
**ADRs referenced**: ADR-005 (Angular), ADR-006 (JWT), ADR-010 (Clean Architecture), ADR-014 (multi-tenant membership)

---

## 1. Business Goal

Establecer el workspace Angular 18 que sirva de base para todas las
features del frontend. El scaffold incluye la estructura de carpetas, la
configuración de Angular Material, los interceptores HTTP, los guards de
ruta, la infraestructura de environments y el flujo de autenticación de dos
pasos (identidad → selección de tenant) definido en ADR-014.

También incluye una **Landing Page pública** que presenta el producto y
dirige al usuario al login, evitando redirigir a visitantes no autenticados
a una pantalla de error.

---

## 2. Functional Requirements

- FR-01: El workspace Angular 18 debe estar ubicado en `src/frontend/` y
  compilar sin errores con `ng build --configuration=production`.
- FR-02: La estructura de carpetas debe seguir `docs/standards/02-frontend-standards.md`
  (core / shared / features / layout).
- FR-03: Angular Material 18 debe estar instalado y configurado con un tema
  custom (paleta azul médica, modo claro por defecto).
- FR-04: `app.config.ts` debe proveer `provideRouter`, `provideHttpClient`
  (con interceptores), `provideAnimationsAsync` y `provideNativeDateAdapter`.
- FR-05: `AuthInterceptor` debe adjuntar el header `Authorization: Bearer <token>`
  en cada petición saliente hacia `apiBaseUrl` cuando exista accessToken en
  memoria. NO adjuntar en peticiones a URLs externas.
- FR-06: `TenantInterceptor` debe adjuntar `X-Tenant-ID: <tenantId>` en cada
  petición saliente hacia `apiBaseUrl` cuando exista tenantId en el estado
  de auth (estado READY). NO adjuntar si el estado es IDENTITY_CONFIRMED.
- FR-07: `ErrorInterceptor` debe interceptar:
  - 401 → si no está en flujo switch-tenant ni select-tenant: limpiar estado
    y redirigir a `/`.
  - 403 → mostrar snackbar "Sin permiso".
  - 5xx → mostrar snackbar "Error del servidor".
  No redirigir en 401 si `isSwitchingTenant === true` o si la petición es
  a `/auth/select-tenant` o `/auth/switch-tenant`.
- FR-08: `AuthGuard` debe proteger todas las rutas bajo `/app/**`. Requiere
  estado READY. Si UNAUTHENTICATED → redirigir a `/`. Si IDENTITY_CONFIRMED
  → redirigir a `/select-tenant`.
- FR-09: `RoleGuard` debe rechazar acceso a rutas protegidas por rol y
  redirigir a `/app/unauthorized` si el rol del usuario no coincide.
- FR-10: Las rutas deben estar definidas en `app.routes.ts`:
  - `/` → `LandingComponent` (pública, eager).
  - `/login` → `LoginComponent` (pública, eager).
  - `/select-tenant` → `SelectTenantComponent` (requiere estado
    IDENTITY_CONFIRMED; si READY → `/app/agenda`; si UNAUTHENTICATED → `/`).
  - `/app` → shell con `AuthGuard` (requiere READY), hijos lazy:
    `agenda`, `patients`, `clinical`, `reports`, `settings`, `unauthorized`.
- FR-11: `LandingComponent` debe incluir:
  - Un header con el logo/nombre del producto y un botón "Iniciar sesión"
    que navega a `/login`.
  - Una sección hero con el tagline del producto y un CTA secundario
    "Solicitar demo" (placeholder, sin lógica).
  - Al menos dos secciones de características del producto (placeholders).
  - Un footer simple con el nombre del producto.
- FR-12: `SelectTenantComponent` debe mostrar la lista de tenants disponibles
  para el usuario autenticado (proveniente del identity token), permitir
  seleccionar uno y llamar a `AuthService.selectTenant(tenantId)`.
  Si solo hay un tenant, el componente lo selecciona automáticamente sin
  mostrar opciones al usuario.
- FR-13: `MainLayoutComponent` debe incluir:
  - `NavbarComponent` con nombre del tenant activo y un menu de usuario
    con opción "Cambiar clínica" (llama `AuthService.switchTenant()`) si el
    usuario pertenece a más de un tenant, y opción "Cerrar sesión".
  - `SidebarComponent` con los ítems de navegación (placeholders).
  - `<router-outlet>` para el contenido de cada feature.
- FR-14: `AuthService` debe implementar la máquina de estados de ADR-014:
  - `authState = signal<'unauthenticated' | 'identity_confirmed' | 'ready'>('unauthenticated')`
  - `currentUser = signal<User | null>(null)`
  - `availableTenants = signal<TenantMembership[]>([])`
  - `activeTenant = signal<TenantMembership | null>(null)`
  - `isAuthenticated = computed(() => authState() === 'ready')`
  - `login(credentials)` → llama `/auth/login`, guarda identityToken, setea
    `authState = 'identity_confirmed'`. Si un solo tenant → llama
    `selectTenant()` automáticamente.
  - `selectTenant(tenantId)` → llama `/auth/select-tenant`, guarda
    accessToken, setea `authState = 'ready'`.
  - `switchTenant(tenantId)` → setea `isSwitchingTenant = true`, llama
    `/auth/switch-tenant`, actualiza token y tenant activo, setea
    `isSwitchingTenant = false`.
  - `logout()` → llama `/auth/logout`, limpia todo el estado.
- FR-15: `environments/environment.ts` y `environments/environment.prod.ts`
  deben exponer `apiBaseUrl` y `production` (boolean).
- FR-16: El `Dockerfile` de `src/frontend/` debe compilar con `ng build`
  en stage de build y servir el bundle estático desde nginx:alpine en stage
  final. (Ya existe — verificar que compila con el nuevo workspace.)

---

## 3. Non-Functional Requirements

- NFR-01: `ng build --configuration=production` debe completar sin errores ni
  warnings TypeScript de tipo `error TS`.
- NFR-02: Bundle inicial (`main.js`) debe ser < 500 KB gzipped.
- NFR-03: Ningún token (accessToken, identityToken) debe almacenarse en
  `localStorage` ni `sessionStorage` — solo en memoria (signals de `AuthService`).
- NFR-04: Toda clase y método público debe tener JSDoc de una línea mínima.
- NFR-05: Ningún componente debe superar 150 líneas de template + clase.

---

## 4. Acceptance Criteria

- AC-01: Dado un workspace limpio, cuando se ejecuta `ng build --configuration=production`,
  entonces el comando termina con código 0 y genera `dist/frontend/`.
- AC-02: Dado un usuario sin sesión, cuando navega a `/app/agenda`,
  entonces `AuthGuard` redirige a `/`.
- AC-03: Dado un usuario en estado IDENTITY_CONFIRMED, cuando navega a `/app/agenda`,
  entonces `AuthGuard` redirige a `/select-tenant`.
- AC-04: Dado un usuario en estado IDENTITY_CONFIRMED con un solo tenant,
  entonces `AuthService.login()` llama `selectTenant()` automáticamente y
  el estado pasa a READY sin mostrar la pantalla de selección.
- AC-05: Dado un usuario en estado READY con accessToken en memoria,
  cuando `AuthInterceptor` procesa una petición a `apiBaseUrl`, entonces
  el header `Authorization: Bearer <token>` está presente.
- AC-06: Dado un usuario en estado READY con tenantId, cuando `TenantInterceptor`
  procesa una petición, entonces el header `X-Tenant-ID: <tenantId>` está
  presente.
- AC-07: Dado un backend que responde 401, cuando `ErrorInterceptor` lo
  recibe fuera del flujo switch/select-tenant, entonces limpia el estado
  de auth y redirige a `/`.
- AC-08: Dado un usuario con rol SECRETARY, cuando navega a una ruta
  protegida por rol DOCTOR, entonces `RoleGuard` redirige a `/app/unauthorized`.
- AC-09: Dado un usuario autenticado en READY, cuando carga la app, entonces
  `MainLayoutComponent` muestra navbar con nombre del tenant activo, sidebar y
  router-outlet funcional.
- AC-10: Dado el visitante en `/`, entonces `LandingComponent` muestra el
  header con botón "Iniciar sesión" y las secciones de presentación del producto.
- AC-11: Dado un usuario con 2+ tenants en el navbar, cuando hace clic en
  "Cambiar clínica", entonces `AuthService.switchTenant()` es invocado y el
  estado READY se actualiza con el nuevo tenant.

---

## 5. Edge Cases

- EC-01: Token expirado pero no nulo en memoria → `ErrorInterceptor` recibe
  401 → limpia estado → redirige a `/` (no loop infinito).
- EC-02: Petición a URL externa (no `apiBaseUrl`) → los interceptores de auth
  y tenant NO deben adjuntar headers.
- EC-03: Refresh de página (F5) → todos los tokens en memoria se pierden →
  `AuthGuard` redirige a `/` correctamente (no pantalla rota).
- EC-04: Múltiples peticiones concurrentes con token expirado → solo una
  redirección a `/`, no múltiples (flag `isRedirecting` en `ErrorInterceptor`).
- EC-05: Usuario con un solo tenant → `login()` pasa directamente a READY,
  `SelectTenantComponent` nunca se muestra.
- EC-06: `switchTenant()` en progreso → peticiones en vuelo reciben 401 →
  `ErrorInterceptor` ignora el 401 (flag `isSwitchingTenant`) y no redirige.
- EC-07: Usuario navega manualmente a `/select-tenant` estando en READY →
  redirigir a `/app/agenda`.

---

## 6. Constraints

- Usar Angular 18 standalone components (sin NgModules).
- Angular Material 18 — no instalar librerías de UI alternativas.
- State management: Angular Signals únicamente — no NgRx, no RxJS BehaviorSubject
  para estado global.
- Ningún token toca `localStorage` / `sessionStorage` (NFR-03).
- El `Dockerfile` existente en `src/frontend/` debe reutilizarse sin reescribirse
  si ya es correcto.

---

## 7. Dependencies

| Dependencia | Tipo | Notas |
|---|---|---|
| T-001 (scaffold backend) | Interno | `apiBaseUrl` apunta al backend de T-001 |
| T-003 (auth JWT backend) | Interno | `AuthService` llama endpoints que se implementan en T-003; aquí solo el skeleton |
| ADR-005 | Decisión | Stack Angular 18 + Material |
| ADR-006 | Decisión | JWT stateless — token en memoria |
| ADR-014 | Decisión | Multi-tenant membership — flujo two-step login + tenant switcher |
| `src/frontend/Dockerfile` | Infra | Multi-stage build ya definido en T-001 |

---

## 8. Risks

- R-01: Versión de Node.js incompatible con Angular 18 en el Dockerfile →
  Mitigación: fijar `node:20-alpine` como base del build stage.
- R-02: Tamaño del bundle inicial excede 500 KB por Material completo →
  Mitigación: importar solo los módulos de Material necesarios por feature.
- R-03: El identity token expira (5 min) antes de que el usuario seleccione
  tenant en conexión lenta → Mitigación: `SelectTenantComponent` maneja el
  error 401 de `/auth/select-tenant` mostrando un prompt de re-login.

---

## 9. Open Questions

Sin preguntas abiertas.

---

## 10. Domain Design Notes

```
src/frontend/src/app/
├── core/
│   ├── auth/
│   │   ├── auth.service.ts          → máquina de estados (ADR-014)
│   │   ├── auth.guard.ts            → requiere estado READY
│   │   ├── select-tenant.guard.ts   → requiere estado IDENTITY_CONFIRMED
│   │   ├── role.guard.ts
│   │   └── models/
│   │       ├── user.model.ts        → { id, email, fullName }
│   │       ├── tenant-membership.model.ts → { tenantId, tenantName, role }
│   │       └── token.model.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts
│   │   ├── tenant.interceptor.ts
│   │   └── error.interceptor.ts
│   ├── tenant/
│   │   └── tenant.service.ts        → wrapper sobre AuthService.activeTenant()
│   └── api/
│       └── auth.api.ts              → login / select-tenant / switch-tenant / logout
├── shared/                          → vacío — se puebla por feature
├── features/
│   ├── landing/                     → LandingComponent (eager)
│   ├── select-tenant/               → SelectTenantComponent (eager)
│   └── [agenda|patients|clinical|reports|settings|unauthorized]/  → lazy
├── layout/
│   ├── main-layout/
│   ├── navbar/                      → tenant switcher si availableTenants().length > 1
│   └── sidebar/
├── app.component.ts
├── app.config.ts
└── app.routes.ts
```

### AuthService state machine

```
UNAUTHENTICATED ──login()──► IDENTITY_CONFIRMED ──selectTenant()──► READY
                                                                      │
                                                       switchTenant() ◄┘
READY ──logout()──► UNAUTHENTICATED
```

---

## 11. Test Cases

| ID | Maps to | Tipo | Descripción |
|---|---|---|---|
| TC-01 | AC-02 | Unit | `AuthGuard` redirige a `/` cuando estado es UNAUTHENTICATED |
| TC-02 | AC-03 | Unit | `AuthGuard` redirige a `/select-tenant` cuando estado es IDENTITY_CONFIRMED |
| TC-03 | AC-05 | Unit | `AuthInterceptor` adjunta header Authorization cuando accessToken presente |
| TC-04 | AC-06 | Unit | `TenantInterceptor` adjunta X-Tenant-ID cuando estado READY |
| TC-05 | AC-06 | Unit | `TenantInterceptor` NO adjunta X-Tenant-ID cuando estado IDENTITY_CONFIRMED |
| TC-06 | AC-07 | Unit | `ErrorInterceptor` llama `logout()` y navega a `/` en respuesta 401 normal |
| TC-07 | AC-08 | Unit | `RoleGuard` redirige a `/unauthorized` cuando rol no coincide |
| TC-08 | EC-02 | Unit | Interceptores NO adjuntan headers en peticiones a URLs externas |
| TC-09 | EC-04 | Unit | `ErrorInterceptor` no redirige dos veces con flag `isRedirecting` |
| TC-10 | EC-06 | Unit | `ErrorInterceptor` ignora 401 cuando `isSwitchingTenant === true` |
| TC-11 | AC-04 | Unit | `AuthService.login()` llama `selectTenant()` automáticamente si un solo tenant |
| TC-12 | EC-07 | Unit | `SelectTenantGuard` redirige a `/app/agenda` si estado ya es READY |
| TC-13 | AC-01 | E2E | `ng build --configuration=production` termina con código 0 |
