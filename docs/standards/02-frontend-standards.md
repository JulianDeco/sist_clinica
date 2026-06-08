# Frontend Standards — Angular 18 + TypeScript + Angular Material

---

## 1. Feature-Based Folder Structure

```
src/
├── app/
│   ├── core/                          # Singleton services, loaded once in AppComponent
│   │   ├── auth/
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.guard.ts
│   │   │   ├── role.guard.ts
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       └── token.model.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   ├── tenant.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   ├── tenant/
│   │   │   └── tenant.service.ts
│   │   └── api/                       # Generated or hand-written API clients
│   │       ├── appointments.api.ts
│   │       ├── patients.api.ts
│   │       └── auth.api.ts
│   │
│   ├── shared/                        # Reusable, feature-agnostic elements
│   │   ├── components/
│   │   │   ├── confirmation-dialog/
│   │   │   ├── loading-spinner/
│   │   │   ├── empty-state/
│   │   │   └── status-badge/
│   │   ├── directives/
│   │   │   ├── has-permission.directive.ts
│   │   │   └── autofocus.directive.ts
│   │   ├── pipes/
│   │   │   ├── date-local.pipe.ts
│   │   │   └── full-name.pipe.ts
│   │   └── models/
│   │       ├── api-response.model.ts
│   │       └── page.model.ts
│   │
│   ├── features/                      # Lazy-loaded feature areas
│   │   ├── agenda/
│   │   │   ├── agenda.routes.ts       # Route definitions
│   │   │   ├── components/
│   │   │   │   ├── agenda-calendar/
│   │   │   │   └── appointment-form/
│   │   │   ├── services/
│   │   │   │   └── agenda.service.ts
│   │   │   ├── store/                 # Signals-based state
│   │   │   │   └── agenda.store.ts
│   │   │   └── models/
│   │   │       └── appointment.model.ts
│   │   ├── patients/
│   │   ├── clinical/
│   │   ├── reports/
│   │   └── settings/
│   │
│   ├── layout/                        # Shell: navbar, sidebar, footer
│   │   ├── main-layout/
│   │   ├── navbar/
│   │   └── sidebar/
│   │
│   ├── app.component.ts
│   ├── app.config.ts                  # provideRouter, provideHttpClient, etc.
│   └── app.routes.ts                  # Top-level routes with lazy imports
│
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
└── assets/
```

---

## 2. Core Module

`core/` is **imported once** (in `app.config.ts`). It contains:

- `AuthService` — login, logout, token refresh, current user signal
- `TenantService` — current tenant ID, tenant config
- `AuthGuard` — blocks unauthenticated navigation
- `RoleGuard` — checks required role before activating route
- `AuthInterceptor` — attaches `Authorization: Bearer <token>` header
- `TenantInterceptor` — attaches `X-Tenant-ID` header
- `ErrorInterceptor` — handles 401 (redirect to login), 403, 500 globally
- API client services (`appointments.api.ts`, etc.)

**Never import `core/` services from `shared/` or from another feature.**

---

## 3. Shared Module

`shared/` contains reusable UI elements with **no business logic**:

- Components used in ≥2 features
- Generic directives and pipes
- Shared models/interfaces that multiple features reference

**Rule**: A shared component must not depend on any feature service.

---

## 4. Feature Module Structure

Every feature follows this internal structure:

```
features/agenda/
├── agenda.routes.ts         # Lazy-loaded route config
├── components/              # Presentational and container components
├── services/                # Feature-scoped services (providedIn: 'root' or injected via routes)
├── store/                   # Signals-based state (see §5)
└── models/                  # TypeScript interfaces/types for this feature
```

---

## 5. State Management Strategy (Angular Signals)

Use **Angular Signals** (native, no NgRx). Pattern per feature:

```typescript
// agenda.store.ts
@Injectable({ providedIn: 'root' })
export class AgendaStore {
    // state signals
    private readonly _appointments = signal<Appointment[]>([]);
    private readonly _loading = signal(false);
    private readonly _error = signal<string | null>(null);

    // computed (derived state)
    readonly appointments = this._appointments.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly todayAppointments = computed(() =>
        this._appointments().filter(a => isToday(a.date)));

    // actions
    loadAppointments(date: Date): void {
        this._loading.set(true);
        this.agendaApi.getByDate(date).subscribe({
            next: (data) => {
                this._appointments.set(data);
                this._loading.set(false);
            },
            error: (err) => {
                this._error.set(err.message);
                this._loading.set(false);
            }
        });
    }
}
```

**Rules:**
- No NgRx, no BehaviorSubject for new code
- One store per feature
- Components read from store; never call API services directly from components

---

## 6. API Client Strategy

Each backend controller gets a corresponding API client in `core/api/`:

```typescript
// core/api/appointments.api.ts
@Injectable({ providedIn: 'root' })
export class AppointmentsApi {
    private readonly baseUrl = `${environment.apiUrl}/v1/appointments`;

    constructor(private http: HttpClient) {}

    book(request: BookAppointmentRequest): Observable<AppointmentResponse> {
        return this.http.post<AppointmentResponse>(this.baseUrl, request);
    }

    getByDate(tenantId: string, date: string): Observable<PageResponse<AppointmentResponse>> {
        return this.http.get<PageResponse<AppointmentResponse>>(this.baseUrl, {
            params: { date, tenantId }
        });
    }
}
```

**Rules:**
- API services live in `core/api/`, not in features
- API services only make HTTP calls; no business logic
- Feature stores call API services, not components

---

## 7. Guards

```typescript
// auth.guard.ts — protects all authenticated routes
export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

// role.guard.ts — protects role-restricted routes
export const roleGuard = (requiredRole: string): CanActivateFn => () => {
    const auth = inject(AuthService);
    return auth.hasRole(requiredRole) ? true : router.createUrlTree(['/forbidden']);
};
```

Route configuration:
```typescript
// agenda.routes.ts
export const AGENDA_ROUTES: Routes = [
    {
        path: '',
        canActivate: [authGuard, roleGuard('SECRETARY')],
        component: AgendaPageComponent
    }
];
```

---

## 8. Interceptors

Three interceptors, registered in order in `app.config.ts`:

| Interceptor | Responsibility |
|---|---|
| `AuthInterceptor` | Appends `Authorization: Bearer <token>`; triggers refresh if 401 |
| `TenantInterceptor` | Appends `X-Tenant-ID: <tenantId>` to every API request |
| `ErrorInterceptor` | Translates HTTP errors to user-facing messages; logs unexpected errors |

---

## 9. Component Responsibilities

### Container Components (smart)
- Inject stores; pass data to presentational children via `@Input`
- Handle user events emitted from children via `@Output`
- Navigate programmatically
- Maximum 150 lines — split into sub-components if exceeded

### Presentational Components (dumb)
- Receive data via `@Input` signals
- Emit events via `@Output`
- Zero service injection (except pipes)
- Fully reusable in isolation

---

## 10. When to Create

| Artifact | Create when... |
|---|---|
| Component | A distinct UI unit with its own template (always) |
| Service | Logic shared by ≥2 components, or HTTP calls |
| Directive | Behavior added to existing elements (e.g. `hasPermission`) |
| Pipe | Transforming display values (dates, names, currencies) |
| Store | A feature needs state shared across ≥2 components |
| Guard | Route requires authentication or authorization check |
| Interceptor | Cross-cutting HTTP behavior (auth headers, error handling) |

---

## 11. TypeScript Conventions

- Strict mode enabled: `"strict": true` in `tsconfig.json`
- No `any` — use `unknown` and narrow with type guards
- Interfaces for API response shapes; `type` aliases for unions/intersections
- `readonly` on all properties that should not be mutated
- Prefer `const` over `let`; never `var`

---

## 12. Angular Material Usage

- Use Angular Material as the **only** component library
- Never mix Material with another UI kit in the same view
- Theming via `@angular/material/theming` — define brand palette in `styles.scss`
- Custom components extend or compose Material components; do not override Material internals

```scss
// styles.scss
@use '@angular/material' as mat;
@include mat.all-component-themes($clinica-theme);
```
