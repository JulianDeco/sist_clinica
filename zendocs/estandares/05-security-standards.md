# Security Standards — Spring Security + JWT

---

## 1. Authentication Approach

**Stateless JWT authentication** — no server-side session state.

Flow:
```
1. POST /api/v1/auth/login  { email, password }
   → Validates credentials against BCrypt hash
   → Returns: { access_token (JWT), expires_in }
   → Sets: refresh_token in httpOnly + Secure + SameSite=Strict cookie

2. All subsequent requests:
   → Authorization: Bearer <access_token>
   → JwtAuthenticationFilter validates token, loads SecurityContext

3. Token refresh:
   → POST /api/v1/auth/refresh  (cookie sent automatically)
   → Validates refresh token in DB (single-use rotation)
   → Returns new access_token; rotates refresh_token cookie
```

---

## 2. JWT Strategy

### Access Token

| Claim | Value | Notes |
|---|---|---|
| `sub` | `{userId}` | UUID string |
| `tenant_id` | `{tenantId}` | UUID string — REQUIRED in every token |
| `role` | `DOCTOR` / `SECRETARY` / `ADMIN` | Single role per session |
| `iss` | `clinicasaas` | Issuer constant |
| `iat` | Unix timestamp | Issued at |
| `exp` | `iat + 1800` | 30 minutes |
| `jti` | UUID | JWT ID — used for revocation |

**What is NOT in the JWT:**
- Permissions list (fetched from Redis/DB on demand)
- Patient data
- Tenant config

Rationale: permissions change frequently; embedding them in a 30-min token
causes stale authorization. Load from Redis cache (5-min TTL) instead.

### Token Signing

- Algorithm: **HS256** (minimum); **RS256** preferred for production
  (allows public key distribution for future microservice validation)
- Secret: minimum 512-bit random key, stored in `JWT_SECRET` env var
- Never hardcode secrets

### Access Token Library

Use **JJWT** (`io.jsonwebtoken:jjwt-api`):
```java
// Token creation
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

## 3. Refresh Token Strategy

- Stored in PostgreSQL `refresh_tokens` table as **SHA-256 hash** (never plaintext)
- Single-use rotation: each use generates a new token and invalidates the old one
- TTL: 7 days
- Delivered via `httpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh` cookie
- On suspicious reuse (token already marked `revoked = true`):
  - Revoke ALL tokens for that user
  - Force re-authentication
  - Log security event

```java
public TokenResult refresh(String rawRefreshToken) {
    String hashed = sha256(rawRefreshToken);
    RefreshToken stored = refreshTokenRepository.findByHashOrThrow(hashed);

    if (stored.isRevoked()) {
        refreshTokenRepository.revokeAllForUser(stored.getUserId()); // revoke family
        throw new RefreshTokenReusedException();
    }
    if (stored.isExpired()) throw new RefreshTokenExpiredException();

    stored.revoke();                          // single-use: invalidate old
    RefreshToken newToken = issueNewToken(...); // rotate
    refreshTokenRepository.save(stored);
    refreshTokenRepository.save(newToken);
    return buildTokenResult(newToken);
}
```

---

## 4. Authorization Approach

**Role-Based with Permission Checks** (hybrid):

- JWT carries `role` (coarse authorization — allows entering a feature area)
- Fine-grained permission checks via Spring Security `@PreAuthorize` + Redis cache

```java
// Controller method level
@PreAuthorize("hasAuthority('APPOINTMENT_CREATE')")
@PostMapping
public ResponseEntity<AppointmentResponse> book(...) { ... }

// Or via custom annotation
@RequiresPermission("APPOINTMENT_CREATE")
```

Permission loading:
```java
@Service
public class PermissionCacheService implements UserDetailsService {
    public UserDetails loadUserByUsername(String userId) {
        // 1. Check Redis cache (clinica:{tenantId}:perms:{userId})
        // 2. If miss → load from DB → store in Redis with 5-min TTL
        // 3. Return UserDetails with GrantedAuthority list
    }
}
```

---

## 5. Password Handling

- BCrypt with **strength 12** (≈ 300ms per hash on modern hardware)
- Never log passwords, never store plaintext
- Password reset via time-limited (15 min) single-use token sent to email
- Minimum password policy (enforced by Bean Validation): 8 characters

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## 6. Secret Management

| Secret | Storage |
|---|---|
| `JWT_SECRET` | Environment variable |
| `DB_PASSWORD` | Environment variable |
| `REDIS_PASSWORD` | Environment variable |
| `SMTP_PASSWORD` | Environment variable |
| `WHATSAPP_API_KEY` | Environment variable |

Rules:
- No secrets in `application.yml` committed to Git
- Use `.env` locally (gitignored); `.env.example` with placeholder values committed
- Docker Compose reads from `.env`
- Production: inject via Docker secrets or VPS environment

---

## 7. Spring Security Filter Chain Order

```
Request
  │
  ▼
CorsFilter                    → Validates CORS headers
  ▼
JwtAuthenticationFilter       → Validates JWT, populates SecurityContext
  ▼
TenantContextFilter           → Extracts tenant_id claim, sets ThreadLocal
  ▼
AuthorizationFilter           → Checks @PreAuthorize / access rules
  ▼
Controller
```

---

## 8. CORS Configuration

Allowed origins: explicit list from `ALLOWED_ORIGINS` env var.
Wildcard `*` is **never allowed** in production.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);    // from env var
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-ID"));
    config.setAllowCredentials(true);            // required for httpOnly cookie
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## 9. Security Headers

Configured via Spring Security:
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

## 10. Sensitive Endpoints

| Endpoint | Protection |
|---|---|
| `POST /api/v1/auth/login` | Rate-limited (5 req/min per IP); BCrypt timing-safe compare |
| `POST /api/v1/auth/refresh` | httpOnly cookie required; DB token validation |
| `DELETE /api/v1/auth/logout` | Revokes refresh token; adds JTI to Redis blocklist |
| `GET /actuator/**` | Restricted to internal network only (Nginx config) |
| `GET /api/v1/admin/**` | Requires `ADMIN` role |
