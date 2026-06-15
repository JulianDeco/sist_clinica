# ADR-014: Membresía Multitenant — Identidad Separada de la Pertenencia

**Estado**: ACCEPTED
**Fecha**: 2026-06-09
**Autor**: Julián Deco
**Supersede**: ninguno — extiende ADR-003 (multitenancy por fila) y ADR-006 (auth JWT)
**Relaciona con**: T-002, T-003, T-004

---

## Contexto

El esquema original (`V003__create_users.sql`) modeló `users` como una entidad
con alcance de tenant: cada fila tiene una FK `tenant_id`, y `email` tiene una
restricción UNIQUE global. Esto funciona para un usuario de un solo tenant pero
rompe el caso real donde **una persona trabaja en múltiples clínicas** (por ejemplo,
un médico con dos consultorios, una secretaria compartida). Bajo el modelo antiguo,
esa persona necesitaría dos cuentas con dos emails, lo que es mala UX y duplica datos
de identidad.

ADR-006 embebe `tenant_id` y `role` directamente en el token de acceso JWT, lo
que asumía una relación 1:1 entre usuario y tenant. Es necesario revisar tanto el
modelo de datos como el flujo de autenticación.

---

## Decisión

**Separar identidad de membresía.**

### Modelo de datos

```
users (identidad global)
  id, email, password_hash, full_name, active, ...

user_tenants (membresía — una fila por usuario × tenant)
  user_id  → users.id
  tenant_id → tenants.id
  role_id   → roles.id
  active    BOOLEAN
  joined_at TIMESTAMPTZ
  PRIMARY KEY (user_id, tenant_id)
```

`users.email` permanece globalmente único — una cuenta por persona real.
`users` no tiene columna `tenant_id`.
`user_roles` (pivot antiguo) se reemplaza por `user_tenants.role_id` — un usuario
tiene exactamente un rol por tenant (modelo más simple para el MVP; extender más
adelante si es necesario).

### Flujo de autenticación (dos pasos)

```
Paso 1 — Token de identidad
  POST /api/v1/auth/login { email, password }
  → 200 { identityToken, tenants: [{ tenantId, tenantName, role }] }
       identityToken: JWT de corta duración (5 min), claims: { sub, purpose:"tenant-select" }
  → Si tenants.length === 1: proceder directamente como si el Paso 2 hubiera sido invocado.

Paso 2 — Token de sesión
  POST /api/v1/auth/select-tenant { tenantId }
  Authorization: Bearer <identityToken>
  → 200 { accessToken }  +  Set-Cookie: refreshToken (httpOnly, 7d)
       accessToken: JWT 30 min, claims: { sub, tenant_id, role, jti }

Cambiar tenant (usuario autenticado)
  POST /api/v1/auth/switch-tenant { tenantId }
  Authorization: Bearer <accessToken>
  → 200 { accessToken }  +  refreshToken rotado en cookie
       Token de acceso anterior revocado mediante blocklist JTI en Redis.
```

### Máquina de estados del frontend (AuthService)

```
UNAUTHENTICATED
    → login() exitoso → IDENTITY_CONFIRMED  (identityToken en memoria, lista de tenants)
IDENTITY_CONFIRMED
    → selectTenant()  → READY               (accessToken en memoria, tenantId establecido)
    → auto-selección si un solo tenant
READY
    → switchTenant()  → READY               (nuevo contexto de tenant)
    → logout()        → UNAUTHENTICATED
```

`AuthGuard` requiere estado === READY.
La ruta `/select-tenant` es accesible solo en estado IDENTITY_CONFIRMED.

---

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Mantener `tenant_id` en `users`, permitir emails duplicados por tenant | Rompe el UNIQUE global en email; pesadilla de UX para usuarios compartidos |
| OAuth2 con IdP por tenant | Correcto para SSO empresarial; exceso masivo para clínicas de 1–5 personas |
| `user_roles` separado por tenant (sin pivot `user_tenants`) | Más JOINs, misma semántica — `user_tenants` es más limpio |
| Embeber lista de tenants en el token de acceso | Inflación del token; la lista cambia cuando el usuario se une/sale de un tenant en medio de la sesión |

---

## Consecuencias

**Positivo:**
- Una cuenta, múltiples clínicas — UX real para profesionales compartidos.
- `users` es ahora una identidad global verdadera; todos los datos de tenant están en `user_tenants`.
- Cambiar de clínica no requiere reingresar credenciales.
- Separación clara: token de identidad (probar quién eres) vs token de sesión (probar dónde trabajas).

**Negativo / compromisos:**
- El login en dos pasos agrega un round-trip HTTP para usuarios multitenant (negligible;
  los usuarios de un solo tenant lo omiten automáticamente).
- `V003` debe ser reemplazado por la migración `V010` que elimina `tenant_id` de `users`
  y crea `user_tenants` — cambio que rompe el esquema existente (aceptable en la etapa de
  scaffold, sin datos en producción aún).
- El pivot `user_roles` se elimina en favor de `user_tenants.role_id` — asume un rol por
  tenant por usuario (restricción del MVP; revisar post-MVP si un médico puede ser
  DOCTOR y ADMIN en la misma clínica).

**Riesgos:**
- La ventana del token de identidad (5 min) es ajustada para conexiones lentas → mitigada
  por auto-reintento en `/select-tenant` 401 con prompt de re-login.
- El switch de tenant invalida el token de acceso actual → cualquier solicitud en vuelo
  durante el switch recibe un 401 → `ErrorInterceptor` no debe redirigir al login en 401
  durante el switch (usar un flag `isSwitchingTenant`, mismo patrón que `isRedirecting`).
