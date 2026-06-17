# Estándares de Seguridad — Spring Security + JWT

---

## 1. Enfoque de Autenticación

**Autenticación JWT stateless** — sin estado de sesión del lado del servidor.

Flujo:
```
1. POST /api/v1/auth/login  { email, password }
   → Valida credenciales contra hash BCrypt
   → Retorna: { access_token (JWT), expires_in }
   → Establece: refresh_token en cookie httpOnly + Secure + SameSite=Strict

2. Todas las solicitudes posteriores:
   → Authorization: Bearer <access_token>
   → JwtAuthenticationFilter valida el token, carga el SecurityContext

3. Renovación de token:
   → POST /api/v1/auth/refresh  (cookie enviada automáticamente)
   → Valida el refresh token en la BD (rotación de uso único)
   → Retorna nuevo access_token; rota la cookie de refresh_token
```

---

## 2. Estrategia JWT

### Token de Acceso

| Claim | Valor | Notas |
|---|---|---|
| `sub` | `{userId}` | String UUID |
| `tenant_id` | `{tenantId}` | String UUID — REQUERIDO en cada token |
| `role` | `DOCTOR` / `SECRETARY` / `ADMIN` | Rol único por sesión |
| `iss` | `kuris` | Constante de emisor |
| `iat` | Timestamp Unix | Emitido en |
| `exp` | `iat + 1800` | 30 minutos |
| `jti` | UUID | ID del JWT — usado para revocación |

**Lo que NO va en el JWT:**
- Lista de permisos (se obtienen de Redis/BD bajo demanda)
- Datos del paciente
- Configuración del tenant

Justificación: los permisos cambian frecuentemente; embebidos en un token de 30 min
causan autorización desactualizada. Cargar desde la caché de Redis (TTL de 5 min).

### Firma del Token

- Algoritmo: **HS256** (mínimo); **RS256** preferido para producción
  (permite distribución de clave pública para validación futura en microservicios)
- Secreto: clave aleatoria de mínimo 512 bits, almacenada en la variable de entorno `JWT_SECRET`
- Nunca codificar secretos directamente

### Biblioteca para Token de Acceso

Usar **JJWT** (`io.jsonwebtoken:jjwt-api`):
```java
// Creación del token
String token = Jwts.builder()
    .subject(userId.toString())
    .claim("tenant_id", tenantId.toString())
    .claim("role", role.name())
    .id(UUID.randomUUID().toString())
    .issuedAt(new Date())
    .expiration(Date.from(Instant.now().plus(ACCESS_TOKEN_TTL)))
    .signWith(signingKey)
    .compact();
```

---

## 3. Estrategia de Refresh Token

- Almacenado en la tabla `refresh_tokens` de PostgreSQL como **hash SHA-256** (nunca en texto plano)
- Rotación de uso único: cada uso genera un nuevo token e invalida el anterior
- TTL: 7 días
- Entregado mediante cookie `httpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh`
- Ante reutilización sospechosa (token ya marcado como `revoked = true`):
  - Revocar TODOS los tokens de ese usuario
  - Forzar re-autenticación
  - Registrar evento de seguridad

```java
public TokenResult refresh(String rawRefreshToken) {
    String hashed = sha256(rawRefreshToken);
    RefreshToken stored = refreshTokenRepository.findByHashOrThrow(hashed);

    if (stored.isRevoked()) {
        refreshTokenRepository.revokeAllForUser(stored.getUserId()); // revocar familia
        throw new RefreshTokenReusedException();
    }
    if (stored.isExpired()) throw new RefreshTokenExpiredException();

    stored.revoke();                          // uso único: invalidar el anterior
    RefreshToken newToken = issueNewToken(...); // rotar
    refreshTokenRepository.save(stored);
    refreshTokenRepository.save(newToken);
    return buildTokenResult(newToken);
}
```

---

## 4. Enfoque de Autorización

**Basado en Roles con Verificación de Permisos** (híbrido):

- El JWT lleva `role` (autorización gruesa — permite entrar en un área de feature)
- Verificaciones de permisos granulares mediante `@PreAuthorize` de Spring Security
  + caché de Redis

```java
// A nivel de método en el controlador
@PreAuthorize("hasAuthority('APPOINTMENT_CREATE')")
@PostMapping
public ResponseEntity<AppointmentResponse> book(...) { ... }

// O mediante anotación personalizada
@RequiresPermission("APPOINTMENT_CREATE")
```

Carga de permisos:
```java
@Service
public class PermissionCacheService implements UserDetailsService {
    public UserDetails loadUserByUsername(String userId) {
        // 1. Verificar caché de Redis (clinica:{tenantId}:perms:{userId})
        // 2. Si hay cache miss → cargar desde BD → almacenar en Redis con TTL de 5 min
        // 3. Retornar UserDetails con lista de GrantedAuthority
    }
}
```

---

## 5. Manejo de Contraseñas

- BCrypt con **fuerza 12** (≈ 300ms por hash en hardware moderno)
- Nunca registrar contraseñas, nunca almacenar en texto plano
- Restablecimiento de contraseña mediante token de uso único de tiempo limitado (15 min)
  enviado por email
- Política mínima de contraseñas (aplicada por Bean Validation): 8 caracteres

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## 6. Gestión de Secretos

| Secreto | Almacenamiento |
|---|---|
| `JWT_SECRET` | Variable de entorno |
| `DB_PASSWORD` | Variable de entorno |
| `REDIS_PASSWORD` | Variable de entorno |
| `SMTP_PASSWORD` | Variable de entorno |
| `WHATSAPP_API_KEY` | Variable de entorno |

Reglas:
- Sin secretos en `application.yml` confirmado en Git
- Usar `.env` localmente (en .gitignore); `.env.example` con valores de marcador confirmado
- Docker Compose lee desde `.env`
- Producción: inyectar mediante Docker secrets o entorno del VPS

---

## 7. Orden del Filter Chain de Spring Security

```
Solicitud
  │
  ▼
CorsFilter                    → Valida encabezados CORS
  ▼
JwtAuthenticationFilter       → Valida JWT, popula SecurityContext
  ▼
TenantContextFilter           → Extrae claim tenant_id, establece ThreadLocal
  ▼
AuthorizationFilter           → Verifica @PreAuthorize / reglas de acceso
  ▼
Controlador
```

---

## 8. Configuración CORS

Orígenes permitidos: lista explícita desde la variable de entorno `ALLOWED_ORIGINS`.
El wildcard `*` **nunca está permitido** en producción.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);    // desde variable de entorno
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-ID"));
    config.setAllowCredentials(true);            // requerido para cookie httpOnly
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## 9. Encabezados de Seguridad

Configurados mediante Spring Security:
```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
    .xssProtection(XssProtectionConfig::block)
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
);
```

---

## 10. Endpoints Sensibles

| Endpoint | Protección |
|---|---|
| `POST /api/v1/auth/login` | Rate-limited (5 req/min por IP); comparación BCrypt timing-safe |
| `POST /api/v1/auth/refresh` | Cookie httpOnly requerida; validación de token en BD |
| `DELETE /api/v1/auth/logout` | Revoca el refresh token; agrega JTI a la blocklist de Redis |
| `GET /actuator/**` | Restringido solo a la red interna (configuración de Nginx) |
| `GET /api/v1/admin/**` | Requiere rol `ADMIN` |
