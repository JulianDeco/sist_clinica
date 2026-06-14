# Módulo: Auth / SMART on FHIR

> ⚠️ Estructura legacy del stack FastAPI descartado (ADR-001/ADR-005).
> La estructura vigente es Spring Boot (`config/SecurityConfig.java`) + Angular 18 — ver spec T-003.

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/core/security.py` | Servidor OAuth2 con authlib, generación/verificación JWT |
| `backend/app/core/dependencies.py` | `get_current_user()`, `require_permission()` — en Spring Boot: filtros/method security de Spring Security |
| `backend/app/modules/auth/router.py` | Endpoints `/oauth/login`, `/oauth/authorize`, `/oauth/token`, `/oauth/revoke` |
| `backend/app/modules/auth/models.py` | SQLAlchemy: `User`, `RefreshToken`, `AuthorizationCode` |
| `frontend/lib/medplum-client.ts` | `MedplumClient` configurado con `baseUrl=VPS_URL` |
| `frontend/app/(auth)/callback/page.tsx` | Intercambia `code` por tokens (PKCE) |

## API

| Endpoint | Método | Auth requerida | Descripción |
|---|---|---|---|
| `/oauth/login` | POST | No | Valida credenciales, retorna `login_ticket` (120 s, TTL en Redis) |
| `/oauth/authorize` | GET | No | Inicia flujo Authorization Code + PKCE (requiere `login_ticket` + `state`) |
| `/oauth/token` | POST | No | Intercambia code → access_token + refresh_token |
| `/oauth/revoke` | POST | Bearer | Revoca refresh token |
| `/auth/me` | GET | Bearer | Retorna `{id, email, role, tenant_id, permissions[]}` |
| `/.well-known/smart-configuration` | GET | No | Discovery SMART on FHIR |
| `/fhir/R4/metadata` | GET | No | CapabilityStatement FHIR |

## Flujo PKCE completo

```
1. Frontend genera:
   - code_verifier (random 128 bytes, base64url)
   - code_challenge = BASE64URL(SHA256(code_verifier))
   - state = nonce de 128 bits aleatorio → guardar en sessionStorage

2. Usuario ingresa credenciales → POST /oauth/login
   → Backend valida → retorna login_ticket (UUID, TTL 120s en Redis)

3. Frontend redirige a:
   GET /oauth/authorize?response_type=code
                       &client_id=clinica-frontend
                       &redirect_uri=https://app.clinica.com/callback  ← exacta, validada contra whitelist
                       &code_challenge=...
                       &code_challenge_method=S256
                       &state=...
                       &login_ticket=...

4. Backend verifica login_ticket → emite authorization_code (TTL 10 min)
   → Redirect a /callback?code=...&state=...

5. Frontend verifica que state recibido == state guardado en sessionStorage (previene CSRF)

6. POST /oauth/token { code, code_verifier, redirect_uri }
   → Backend verifica code + code_verifier (PKCE) + redirect_uri exacta
   → Emite access_token (JWT, 30 min) + refresh_token (opaco, 7 días, rotation activada)

7. access_token → memoria (React Context)
   refresh_token → httpOnly cookie (Secure; SameSite=Strict; Path=/oauth/token)

8. GET /auth/me con Bearer → guardar permisos granulares en React Context
```

## JWT — Payload y estrategia híbrida

```
JWT contiene:  sub (user_id) + tenant_id + role_name + jti (UUID v4) + exp + iat
Redis contiene: permissions:{tenant_id}:{user_id} → [lista permisos] TTL 5min
               revoked_jtis:{jti} → "1" TTL = tiempo restante del token

BACKEND — frontera de seguridad real:
  1. VERIFICAR firma JWT con algorithms=["HS256"] explícito → nunca aceptar alg:none
  2. Verificar que jti NO esté en revoked_jtis:{jti} → si está = 401 inmediato
  3. Extraer user_id y tenant_id del payload YA VERIFICADO
  4. SET LOCAL app.current_tenant = tenant_id (activa RLS de PostgreSQL)
  5. Redis GET permissions:{tenant_id}:{user_id} → lista permisos
  6. Si cache miss → BD (filtrado por tenant_id) → guardar Redis TTL 5min
  7. require_permission("appointment", "write") → OK / 403

FRONTEND — solo UX, nunca seguridad:
  - jwtDecode(token) decodifica payload SIN verificar firma — correcto
  - Usar role_name y tenant_id solo para mostrar/ocultar UI
  - Permisos granulares: GET /auth/me al login → guardar en React Context
  - Si alguien manipula el JWT local → firma inválida al llamar a la API → 401
```

## Refresh Token Rotation

```
Al usar refresh_token:
  1. Verificar que token NO esté revocado (revoked = false en BD)
  2. Emitir NUEVO access_token + NUEVO refresh_token
  3. Marcar el refresh_token anterior como revoked = true
  4. Si se detecta uso de token ya revocado → revocar TODA la familia
     (indicador de token theft) → forzar re-login del usuario

Revocación de access_token (logout o cambio de contraseña):
  - SET revoked_jtis:{jti} = "1" EX {tiempo_restante_segundos}
  - El backend verifica esta key en cada request
```

## Seguridad del flujo OAuth2

- `redirect_uri`: validación por igualdad EXACTA contra whitelist en BD. Nunca prefix ni wildcard.
- `state`: nonce de 128 bits, guardado en sessionStorage antes del redirect. Verificado en callback antes de intercambiar el code.
- `authorization_code` TTL: máximo 10 minutos.
- Rate limiting en `/oauth/login` y `/oauth/token`: máximo 10 intentos / IP / minuto.
- CORS: solo origins de la whitelist de redirect_uri.

## Patrones Clave

- PKCE obligatorio para el frontend (Angular 18 en VPS propio)
- Access token: 30 min + jti revocable; Refresh token: 7 días con rotation
- Frontend guarda access_token en memoria (no localStorage/sessionStorage)
- Refresh token en httpOnly cookie: `Secure; SameSite=Strict; Path=/oauth/token`
- `platform_admin`: acceso cross-tenant (solo operador de la plataforma)
- `clinic_admin`: super_admin de un tenant específico — NO tiene acceso a otros tenants

## NO HACER

- No poner lista completa de permisos en el JWT
- No almacenar access_token en localStorage (XSS risk)
- No validar redirect_uri con prefix match — solo igualdad exacta
- No omitir el parámetro `state` en el flujo authorize
- No usar `algorithms=None` o sin especificar al decodificar JWT
- No emitir authorization_code con TTL mayor a 10 minutos

## Dependencias

→ `.claude/context/modules/02-rbac.md` (permisos y roles)
→ `.claude/context/modules/08-database.md` (modelos User, RefreshToken, audit_log)
