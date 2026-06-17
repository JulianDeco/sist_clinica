# ADR-006: Autenticación JWT Stateless con Rotación de Refresh Token

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, seguridad

---

## Contexto

Kuris es una API REST stateless. La autenticación debe ser: escalable
(sin almacén de sesiones del lado del servidor), segura (protección contra robo
de tokens), y práctica (sin requerir re-login cada 30 minutos).

## Decisión

Usar **tokens de acceso JWT stateless** (TTL de 30 minutos) combinados con
**rotación de refresh token de uso único** (TTL de 7 días, almacenado con hash en
PostgreSQL, entregado mediante cookie httpOnly).

Los claims del JWT incluyen: `sub` (userId), `tenant_id`, `role`, `jti` (para
revocación). Los permisos NO están embebidos en el JWT — se cargan desde la caché
de Redis bajo demanda para evitar datos de autorización desactualizados.

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Sesiones del lado del servidor | Requiere almacén de sesiones; menos escalable; no idiomático para REST |
| JWT de larga duración (7 días, sin refresh) | No se puede revocar al cerrar sesión o ante una vulneración |
| OAuth2 Authorization Code + PKCE | Correcto para delegación de terceros; no necesario para SPA + API de mismo origen |

## Consecuencias

**Positivo:**
- Stateless — no se requiere BD de sesiones
- Los tokens de acceso de corta duración limitan la ventana de exposición a 30 minutos
- Rotación de refresh token: reemisión automática + revocación del token anterior
  previene el robo silencioso
- Cookie httpOnly para el refresh token: no accesible por JavaScript (protección XSS)

**Negativo / compromisos:**
- El token de acceso no puede revocarse en su vida útil (30 min) sin una blocklist
  JTI en Redis — la blocklist JTI se agrega para el caso de uso de logout
- La rotación del refresh token requiere una escritura en PostgreSQL en cada
  actualización (aceptable)

**Riesgos:**
- Refresh token robado antes de la rotación: mitigado detectando reutilización
  (revocar toda la familia de tokens)
