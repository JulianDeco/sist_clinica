# Módulo: RBAC Dinámico

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/core/rbac.py` | Middleware: lee permisos desde Redis, fallback a BD |
| `backend/app/modules/rbac/router.py` | CRUD roles/permisos (solo `clinic_admin` o `platform_admin`) |
| `backend/app/modules/rbac/models.py` | `Role`, `Permission`, `RolePermission`, `UserRole` |
| `backend/app/modules/rbac/service.py` | `get_user_permissions()`, invalidación cache |

## API — Endpoints RBAC

| Endpoint | Método | Quién puede | Descripción |
|---|---|---|---|
| `/admin/roles` | GET/POST | clinic_admin (propio tenant) | Listar y crear roles |
| `/admin/roles/{id}` | PUT/DELETE | clinic_admin (propio tenant) | Editar/eliminar rol |
| `/admin/roles/{id}/permissions` | POST/DELETE | clinic_admin | Asignar/quitar permiso a rol |
| `/admin/users/{id}/roles` | POST/DELETE | clinic_admin | Asignar/quitar rol a usuario |
| `/platform/tenants` | GET | platform_admin | Ver todos los tenants (cross-tenant) |

Todos los endpoints `/admin/*` filtran automáticamente por el `tenant_id` del JWT — un `clinic_admin` nunca puede ver ni modificar usuarios de otro tenant.

## Tipos de administrador

```
platform_admin  → operador de la plataforma (Julián / equipo técnico)
                  acceso cross-tenant para soporte y operaciones
                  NO debe usarse para operaciones de negocio de una clínica

clinic_admin    → admin de una clínica específica
                  acceso limitado a su propio tenant_id
                  puede gestionar roles, usuarios y configuración de su clínica
```

## Modelo de permisos

```
Permission.resource = "appointment" | "patient" | "encounter" | "condition" |
                      "medication_request" | "family_history" | "coverage" |
                      "obra_social" | "stock" | "report" | "role" | "*"
Permission.action   = "read" | "write" | "delete" | "*"

Ejemplo: ("appointment", "write") → puede crear/editar turnos
```

## Roles iniciales — seed en migración 001

| Rol | Permisos (resource, action) |
|---|---|
| `platform_admin` | `("*", "*")` — cross-tenant |
| `clinic_admin` | `("*", "*")` — solo su tenant |
| `medico` | patient.read, patient.write, appointment.read, appointment.write, encounter.write, condition.write, medication_request.write, family_history.read, family_history.write |
| `recepcionista` | patient.read, patient.write, appointment.read, appointment.write |

Nota: `rw` se expresa como dos permisos separados (`read` + `write`) en la tabla `permissions`.

## Patrones Clave

- Cache Redis: `permissions:{tenant_id}:{user_id}` → JSON list, TTL 5 min
- Al cambiar rol → `invalidate_permissions_cache(tenant_id, user_ids_affected)`
- `require_permission(resource, action)` recibe también `tenant_id` del JWT → en Spring Boot: method security / filtro de Spring Security
- `platform_admin` bypasa cache y re-verifica BD en cada request
- `clinic_admin` usa cache normal pero sus queries siempre están filtradas por `tenant_id`

## Flujo de verificación de permisos

```
request llega con JWT válido (firma verificada, jti no revocado)
  ↓
extraer tenant_id + user_id del JWT
  ↓
SET LOCAL app.current_tenant = tenant_id  (activa RLS PostgreSQL)
  ↓
Redis GET permissions:{tenant_id}:{user_id}
  ↓ cache hit          ↓ cache miss
verificar permiso    BD: SELECT permissions WHERE user_id AND tenant_id
  ↓                    → Redis SET permissions:{tenant_id}:{user_id} EX 300
OK / 403
```

## NO HACER

- No hardcodear chequeos de rol por nombre (`if user.role == "medico"`)
- No usar el JWT para checar permisos específicos — solo `role_name` para identidad UX
- No eliminar roles base desde el panel de admin
- No omitir `tenant_id` en la key de Redis — `permissions:{user_id}` sin tenant es inseguro
- No permitir que un `clinic_admin` acceda a endpoints de otro tenant

## Dependencias

→ `.claude/context/modules/01-auth.md` (JWT híbrido, tenant_id en payload)
→ `.claude/context/modules/08-database.md` (tablas roles/permissions, RLS)
