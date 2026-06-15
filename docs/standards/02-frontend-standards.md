# Estándares de Frontend — Angular 18 + TypeScript + Angular Material

---

## 1. Estructura de Carpetas por Feature

```
src/
├── app/
│   ├── core/                          # Servicios singleton, cargados una vez en AppComponent
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
│   │   └── api/                       # Clientes de API generados o escritos a mano
│   │       ├── appointments.api.ts
│   │       ├── patients.api.ts
│   │       └── auth.api.ts
│   │
│   ├── shared/                        # Elementos reutilizables, agnósticos de feature
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
│   ├── features/                      # Áreas de feature con carga diferida (lazy-loaded)
│   │   ├── agenda/
│   │   │   ├── agenda.routes.ts       # Definición de rutas
│   │   │   ├── components/
│   │   │   │   ├── agenda-calendar/
│   │   │   │   └── appointment-form/
│   │   │   ├── services/
│   │   │   │   └── agenda.service.ts
│   │   │   ├── store/                 # Estado basado en Signals
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
│   └── app.routes.ts                  # Rutas de nivel superior con imports diferidos
│
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
└── assets/
```

---

## 2. Módulo Core

`core/` se **importa una sola vez** (en `app.config.ts`). Contiene:

- `AuthService` — login, logout, renovación de token, signal de usuario actual
- `TenantService` — ID de tenant actual, configuración del tenant
- `AuthGuard` — bloquea la navegación no autenticada
- `RoleGuard` — verifica el rol requerido antes de activar la ruta
- `AuthInterceptor` — adjunta el encabezado `Authorization: Bearer <token>`
- `TenantInterceptor` — adjunta el encabezado `X-Tenant-ID`
- `ErrorInterceptor` — maneja 401 (redirección al login), 403, 500 globalmente
- Servicios de cliente de API (`appointments.api.ts`, etc.)

**Nunca importar servicios de `core/` desde `shared/` ni desde otra feature.**

---

## 3. Módulo Shared

`shared/` contiene elementos de UI reutilizables **sin lógica de negocio**:

- Componentes usados en ≥2 features
- Directivas y pipes genéricos
- Modelos/interfaces compartidos que múltiples features referencian

**Regla**: Un componente compartido no debe depender de ningún servicio de feature.

---

## 4. Estructura Interna del Módulo de Feature

Cada feature sigue esta estructura interna:

```
features/agenda/
├── agenda.routes.ts         # Configuración de rutas con carga diferida
├── components/              # Componentes presentacionales y contenedores
├── services/                # Servicios con alcance de feature (providedIn: 'root' o inyectados por rutas)
├── store/                   # Estado basado en Signals (ver §5)
└── models/                  # Interfaces/tipos TypeScript para esta feature
```

---

## 5. Estrategia de Gestión de Estado (Angular Signals)

Usar **Angular Signals** (nativo, sin NgRx). Patrón por feature:

```typescript
// agenda.store.ts
@Injectable({ providedIn: 'root' })
export class AgendaStore {
    // señales de estado
    private readonly _appointments = signal<Appointment[]>([]);
    private readonly _loading = signal(false);
    private readonly _error = signal<string | null>(null);

    // computed (estado derivado)
    readonly appointments = this._appointments.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly todayAppointments = computed(() =>
        this._appointments().filter(a => isToday(a.date)));

    // acciones
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

**Reglas:**
- Sin NgRx, sin BehaviorSubject para código nuevo
- Un store por feature
- Los componentes leen del store; nunca llaman a servicios de API directamente
  desde componentes

---

## 6. Estrategia de Cliente de API

Cada controlador backend tiene un cliente de API correspondiente en `core/api/`:

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

**Reglas:**
- Los servicios de API viven en `core/api/`, no en features
- Los servicios de API solo hacen llamadas HTTP; sin lógica de negocio
- Los stores de feature llaman a los servicios de API, no los componentes

---

## 7. Guards

```typescript
// auth.guard.ts — protege todas las rutas autenticadas
export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

// role.guard.ts — protege rutas restringidas por rol
export const roleGuard = (requiredRole: string): CanActivateFn => () => {
    const auth = inject(AuthService);
    return auth.hasRole(requiredRole) ? true : router.createUrlTree(['/forbidden']);
};
```

Configuración de rutas:
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

## 8. Interceptores

Tres interceptores, registrados en orden en `app.config.ts`:

| Interceptor | Responsabilidad |
|---|---|
| `AuthInterceptor` | Agrega `Authorization: Bearer <token>`; dispara el refresh si recibe 401 |
| `TenantInterceptor` | Agrega `X-Tenant-ID: <tenantId>` a cada solicitud a la API |
| `ErrorInterceptor` | Traduce errores HTTP a mensajes para el usuario; registra errores inesperados |

---

## 9. Responsabilidades de los Componentes

### Componentes Contenedor (inteligentes)
- Inyectan stores; pasan datos a hijos presentacionales mediante `@Input`
- Manejan eventos emitidos por los hijos mediante `@Output`
- Navegan programáticamente
- Máximo 150 líneas — dividir en subcomponentes si se supera

### Componentes Presentacionales (tontos)
- Reciben datos mediante señales `@Input`
- Emiten eventos mediante `@Output`
- Sin inyección de servicios (excepto pipes)
- Totalmente reutilizables de forma aislada

---

## 10. Cuándo Crear

| Artefacto | Crear cuando... |
|---|---|
| Componente | Una unidad de UI distinta con su propio template (siempre) |
| Servicio | Lógica compartida por ≥2 componentes, o llamadas HTTP |
| Directive | Comportamiento agregado a elementos existentes (por ejemplo, `hasPermission`) |
| Pipe | Transformar valores de visualización (fechas, nombres, monedas) |
| Store | Una feature necesita estado compartido entre ≥2 componentes |
| Guard | La ruta requiere verificación de autenticación o autorización |
| Interceptor | Comportamiento HTTP transversal (encabezados de auth, manejo de errores) |

---

## 11. Convenciones de TypeScript

- Modo estricto habilitado: `"strict": true` en `tsconfig.json`
- Sin `any` — usar `unknown` y acotar con type guards
- Interfaces para formas de respuesta de la API; alias `type` para unions/intersections
- `readonly` en todas las propiedades que no deben mutarse
- Preferir `const` sobre `let`; nunca `var`

---

## 12. Uso de Angular Material

- Usar Angular Material como la **única** biblioteca de componentes
- Nunca mezclar Material con otro kit de UI en la misma vista
- Theming mediante `@angular/material/theming` — definir la paleta de marca en `styles.scss`
- Los componentes personalizados extienden o componen componentes de Material; no sobrescribir
  los internos de Material

```scss
// styles.scss
@use '@angular/material' as mat;
@include mat.all-component-themes($clinica-theme);
```
