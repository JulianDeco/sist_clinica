# Specification: Autenticación JWT — Login, Select-Tenant, Switch-Tenant, Refresh, Logout

**Status**: DRAFT (rev. 2 — 2026-06-10: cookie Path, tenant en refresh token,
endpoints públicos acotados, identity token single-use, X-Forwarded-For)
**Author**: Julian Deco
**Date**: 2026-06-09
**Task**: T-003 (`tasks.json`)
**Branch**: `feature/T-003-auth-jwt`
**Relates to**: T-004 (RBAC), T-011 (frontend login)
**ADRs referenced**: ADR-006 (JWT), ADR-014 (multi-tenant membership)

---

## 1. Business Goal

Implementar el sistema de autenticación stateless completo del backend.
El flujo es de dos pasos (ADR-014): primero se autentica la identidad
(identity token 5 min), luego se selecciona el tenant para obtener el
session token (30 min) con refresh rotation (7 días, httpOnly cookie).
Soporta múltiples tenants por usuario y switch de tenant sin re-login.

---

## 2. Functional Requirements

- FR-01: `POST /api/v1/auth/login` recibe `{ email, password }`, valida
  contra BCrypt (strength 12), y devuelve `{ identityToken, tenants[] }`.
  El `identityToken` es un JWT firmado con claim `purpose = "tenant-select"`
  y TTL de 5 minutos.
- FR-02: `POST /api/v1/auth/select-tenant` recibe `{ tenantId }` con
  `Authorization: Bearer <identityToken>`. Valida que el token sea de
  propósito `tenant-select` y que el usuario pertenezca al tenant.
  Devuelve `{ accessToken }` (JWT 30 min) y setea cookie `refreshToken`
  (httpOnly, Secure, SameSite=Strict, Path=/api/v1/auth, TTL 7d).
  El Path cubre todo `/api/v1/auth` porque `refresh`, `switch-tenant` y
  `logout` necesitan recibir la cookie para rotarla/revocarla.
  Tras emitir el accessToken, el JTI del identityToken se añade al
  blocklist de Redis (single-use: un identityToken no puede generar
  más de una sesión).
- FR-03: `POST /api/v1/auth/switch-tenant` recibe `{ tenantId }` con
  `Authorization: Bearer <accessToken>` válido. Valida que el usuario
  pertenezca al nuevo tenant. Revoca el JTI del accessToken actual en
  Redis (TTL hasta su expiración original). Devuelve nuevo `{ accessToken }`
  y rota la cookie `refreshToken`.
- FR-04: `POST /api/v1/auth/refresh` usa la cookie `refreshToken`
  (httpOnly — no body). Valida el token contra su hash en PostgreSQL
  (single-use rotation): invalida el token usado, genera y persiste uno
  nuevo. Devuelve nuevo `{ accessToken }` y rota la cookie.
  El nuevo accessToken se emite para el `tenant_id` registrado en la fila
  del refresh token (la sesión es por tenant — requiere V011, ver §7);
  el rol se relee de `user_tenants` al momento del refresh.
  Si el token ya estaba revocado → revocar toda la familia del usuario
  y lanzar 401 (reuse attack).
- FR-05: `DELETE /api/v1/auth/logout` con `Authorization: Bearer <accessToken>`.
  Añade el JTI del accessToken al blocklist en Redis (TTL = tiempo
  restante del token). Revoca el refreshToken de la cookie en PostgreSQL.
  Elimina la cookie `refreshToken` (Max-Age=0).
- FR-06: `JwtAuthenticationFilter` (OncePerRequestFilter) extrae el Bearer
  token de cada request, valida firma y expiración, verifica el JTI contra
  el blocklist de Redis, y puebla el `SecurityContext` con un
  `UsernamePasswordAuthenticationToken` que incluye `userId`, `tenantId`,
  `role` y `jti`.
- FR-07: `TenantContextFilter` (OncePerRequestFilter, corre después de
  JwtAuthenticationFilter) extrae `tenant_id` del SecurityContext y lo
  setea en un `TenantContextHolder` (ThreadLocal) para que los repositorios
  lo usen.
- FR-08: El `SecurityConfig` debe dejar públicos únicamente:
  `POST /api/v1/auth/login`, `POST /api/v1/auth/select-tenant` y
  `POST /api/v1/auth/refresh` (select-tenant valida el identityToken en el
  use case — no es un session token; refresh se autentica con la cookie),
  más `GET /actuator/health`. `POST /api/v1/auth/switch-tenant` y
  `DELETE /api/v1/auth/logout` requieren accessToken válido vía
  `JwtAuthenticationFilter`. Todo lo demás requiere autenticación.
- FR-09: Rate limiting en `POST /api/v1/auth/login`: máximo 5 requests
  por IP por minuto. Responde 429 al superar el límite.
  Implementación: contador en Redis con TTL 60s.
  La IP se obtiene de `X-Forwarded-For` (primer valor) porque el backend
  corre detrás de Nginx; Nginx debe sobrescribir el header (nunca
  confiar en el valor enviado por el cliente). Fallback: remote address.
- FR-10: Seed de datos: al menos un usuario ADMIN por tenant en los datos
  de Flyway (o un script de seed separado V011) para poder probar el login
  sin datos manuales.

---

## 3. Non-Functional Requirements

- NFR-01: El secret JWT debe leerse de la variable de entorno `JWT_SECRET`
  (mínimo 512 bits / 64 chars). Nunca hardcodeado.
- NFR-02: BCrypt strength 12.
- NFR-03: El refresh token se almacena como SHA-256 hash en PostgreSQL,
  nunca en texto plano.
- NFR-04: Latencia de `POST /login` < 500ms en condiciones normales
  (BCrypt domina — es esperado ~300ms).
- NFR-05: Latencia de `POST /select-tenant` y `POST /refresh` < 100ms
  (sin BCrypt, solo JWT + Redis + DB).
- NFR-06: Toda clase y método público deben tener JavaDoc de una línea.
- NFR-07: Cobertura de tests ≥ 80% en las clases de la capa application
  e infrastructure de auth.

---

## 4. Acceptance Criteria

- AC-01: Dado email y password correctos, cuando `POST /login`, entonces
  responde 200 con `identityToken` (JWT válido, claim `purpose=tenant-select`,
  TTL 5 min) y lista de tenants del usuario.
- AC-02: Dado email o password incorrectos, cuando `POST /login`, entonces
  responde 401 `{ error: "INVALID_CREDENTIALS" }`.
- AC-03: Dado un `identityToken` válido y un `tenantId` al que pertenece
  el usuario, cuando `POST /select-tenant`, entonces responde 200 con
  `accessToken` (JWT 30 min, claims sub/tenant_id/role/jti) y la cookie
  `refreshToken` está seteada como httpOnly.
- AC-04: Dado un `identityToken` con `purpose` incorrecto o expirado,
  cuando `POST /select-tenant`, entonces responde 401.
- AC-05: Dado un `tenantId` al que el usuario NO pertenece, cuando
  `POST /select-tenant` o `POST /switch-tenant`, entonces responde 403
  `{ error: "TENANT_ACCESS_DENIED" }`.
- AC-06: Dado una cookie `refreshToken` válida, cuando `POST /refresh`,
  entonces responde 200 con nuevo `accessToken` y la cookie es rotada.
- AC-07: Dado la misma cookie `refreshToken` usada dos veces (reuse attack),
  cuando `POST /refresh`, entonces responde 401, todos los refresh tokens
  del usuario son revocados y se registra el evento de seguridad.
- AC-08: Dado un `accessToken` válido, cuando `DELETE /logout`, entonces
  responde 204, el JTI queda en el blocklist Redis y la cookie es eliminada.
- AC-09: Dado el JTI del accessToken en el blocklist Redis, cuando cualquier
  request usa ese token, entonces `JwtAuthenticationFilter` responde 401.
- AC-10: Dado más de 5 intentos de login desde la misma IP en 60 segundos,
  entonces responde 429 `{ error: "RATE_LIMIT_EXCEEDED" }`.
- AC-11: Dado un `accessToken` válido con `tenantId=X`, cuando
  `POST /switch-tenant { tenantId: Y }` (donde el usuario pertenece a Y),
  entonces responde 200 con nuevo accessToken con `tenant_id=Y` y JTI del
  token anterior queda bloqueado en Redis.
- AC-12: Dado un `identityToken` ya usado en un `POST /select-tenant`
  exitoso, cuando se reusa en un segundo `POST /select-tenant`, entonces
  responde 401 (JTI en blocklist — identity token es single-use).

---

## 5. Edge Cases

- EC-01: `identityToken` expirado en `/select-tenant` → 401, frontend
  muestra prompt de re-login.
- EC-02: Usuario sin tenants activos (todos `active=false` en user_tenants)
  → login devuelve 200 con `tenants: []`; el frontend maneja el caso vacío.
- EC-03: Refresh token expirado en BD (fecha `expires_at` < now) → 401
  `{ error: "REFRESH_TOKEN_EXPIRED" }`, sin rotar.
- EC-04: Cookie `refreshToken` ausente en `/refresh` → 401.
- EC-05: Múltiples refresh concurrentes con el mismo token (race condition)
  → el primero en llegar gana; el segundo recibe 401 de reuse attack
  (token ya revocado). Implementar con SELECT FOR UPDATE en la fila del token.
- EC-06: Switch-tenant a un tenant igual al actual → aceptar y rotar igual
  (simplifica el cliente; no es un error).

---

## 6. Constraints

- JJWT 0.12.6 (ya en pom.xml).
- Spring Security 6 (ya en pom.xml).
- Redis para JTI blocklist y rate limiting (ya en pom.xml).
- PostgreSQL para refresh tokens (tablas V003 + V010 + V011).
- V011 (nueva, parte de esta tarea): `ALTER TABLE refresh_tokens ADD COLUMN
  tenant_id UUID NOT NULL REFERENCES tenants(id)` — la sesión de refresh
  es por tenant (sin esto, `/refresh` no sabe para qué tenant emitir el
  accessToken). V010 afirmaba "sin cambios necesarios" en refresh_tokens;
  esa afirmación queda corregida por este spec.
- No usar sesiones HTTP (`SessionCreationPolicy.STATELESS`).
- No usar `spring.jpa.hibernate.ddl-auto=create` — solo Flyway.
- BCrypt strength 12 (no configurable en runtime).

---

## 7. Dependencies

| Dependencia | Tipo | Notas |
|---|---|---|
| V003 + V010 (Flyway) | BD | Tablas `users`, `user_tenants`, `refresh_tokens` |
| V011 (Flyway, nueva en T-003) | BD | `refresh_tokens.tenant_id` — sesión de refresh por tenant |
| T-004 (RBAC) | Interno | Los permisos se cargan en T-004; aquí solo `role` en JWT |
| ADR-006 | Decisión | JWT stateless + refresh rotation |
| ADR-014 | Decisión | Two-step login, identity token vs session token |
| `JWT_SECRET` env var | Config | ≥ 512 bits |
| `ALLOWED_ORIGINS` env var | Config | CORS |

---

## 8. Domain Design

### Paquetes

```
src/backend/src/main/java/com/clinicasaas/
├── domain/
│   └── auth/
│       ├── User.java                     @Entity — tabla users
│       ├── UserTenant.java               @Entity — tabla user_tenants
│       ├── RefreshToken.java             @Entity — tabla refresh_tokens (con tenant_id, V011)
│       ├── UserRepository.java           interface
│       ├── UserTenantRepository.java     interface
│       └── RefreshTokenRepository.java   interface
├── application/
│   └── auth/
│       ├── AuthUseCase.java              interface (login/selectTenant/switchTenant/refresh/logout)
│       └── AuthUseCaseImpl.java          @Service @Transactional
├── infrastructure/
│   ├── auth/
│   │   ├── JpaUserRepository.java        implements UserRepository
│   │   ├── JpaUserTenantRepository.java  implements UserTenantRepository
│   │   └── JpaRefreshTokenRepository.java
│   └── cache/
│       └── JwtBlocklistService.java      Redis — JTI blocklist + rate limit
├── api/
│   └── auth/
│       ├── AuthController.java           @RestController /api/v1/auth/**
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── SelectTenantRequest.java
│       │   ├── SwitchTenantRequest.java
│       │   └── TokenResponse.java
│       └── exception/
│           └── AuthExceptionHandler.java @RestControllerAdvice
└── config/
    ├── SecurityConfig.java               actualizar (ya existe scaffold)
    ├── JwtConfig.java                    @Configuration — JwtService bean
    └── filter/
        ├── JwtAuthenticationFilter.java  OncePerRequestFilter
        ├── TenantContextFilter.java      OncePerRequestFilter
        └── RateLimitFilter.java          OncePerRequestFilter (login only)
```

### JWT claims por tipo de token

| Claim | Identity token | Session token |
|---|---|---|
| `sub` | userId | userId |
| `purpose` | `"tenant-select"` | ausente |
| `tenant_id` | ausente | tenantId |
| `role` | ausente | role name |
| `jti` | UUID | UUID |
| `exp` | now + 5 min | now + 30 min |

### Redis keys

| Key | Valor | TTL |
|---|---|---|
| `auth:blocklist:{jti}` | `"1"` | tiempo restante del token |
| `auth:ratelimit:login:{ip}` | contador | 60 s |

---

## 9. Test Cases

| ID | Maps to | Tipo | Descripción |
|---|---|---|---|
| TC-01 | AC-01 | Unit | `AuthUseCaseImpl.login()` con credenciales válidas → devuelve identityToken + tenants |
| TC-02 | AC-02 | Unit | `AuthUseCaseImpl.login()` con password incorrecto → lanza `InvalidCredentialsException` |
| TC-03 | AC-03 | Unit | `AuthUseCaseImpl.selectTenant()` con identityToken válido → devuelve accessToken |
| TC-04 | AC-04 | Unit | `AuthUseCaseImpl.selectTenant()` con identityToken expirado → lanza `InvalidTokenException` |
| TC-05 | AC-05 | Unit | `AuthUseCaseImpl.selectTenant()` con tenantId no autorizado → lanza `TenantAccessDeniedException` |
| TC-06 | AC-06 | Unit | `AuthUseCaseImpl.refresh()` con refreshToken válido → rota token y devuelve nuevo accessToken |
| TC-07 | AC-07 | Unit | `AuthUseCaseImpl.refresh()` con refreshToken ya revocado → revoca familia y lanza 401 |
| TC-08 | AC-08 | Unit | `AuthUseCaseImpl.logout()` → JTI en Redis blocklist, refreshToken revocado en BD |
| TC-09 | AC-09 | Unit | `JwtAuthenticationFilter` rechaza token con JTI en blocklist → 401 |
| TC-10 | AC-10 | Unit | `RateLimitFilter` bloquea 6º intento de login desde misma IP → 429 |
| TC-11 | AC-11 | Unit | `AuthUseCaseImpl.switchTenant()` → nuevo accessToken con nuevo tenant_id, JTI anterior bloqueado |
| TC-12 | EC-05 | Unit | Dos refreshes concurrentes con mismo token → segundo recibe 401 (SELECT FOR UPDATE) |
| TC-13 | AC-03 | Integration | `POST /api/v1/auth/select-tenant` devuelve cookie httpOnly en respuesta real |
| TC-14 | AC-08 | Integration | `DELETE /api/v1/auth/logout` → token rechazado en siguiente request |
| TC-15 | AC-12 | Unit | `AuthUseCaseImpl.selectTenant()` con identityToken ya usado (JTI en blocklist) → lanza `InvalidTokenException` |
| TC-16 | AC-06 | Unit | `AuthUseCaseImpl.refresh()` emite accessToken con el `tenant_id` persistido en la fila del refresh token |

---

## 10. Open Questions

Sin preguntas abiertas.
