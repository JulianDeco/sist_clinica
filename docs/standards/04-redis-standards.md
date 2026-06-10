# Redis Standards — Redis 8

---

## 1. Allowed Use Cases

Redis is a **cache and ephemeral store only** — never primary storage.

| Use Case | Key Pattern | TTL | Notes |
|---|---|---|---|
| RBAC permission cache | `clinica:{tenantId}:perms:{userId}` | 5 min | Invalidated on role/permission change |
| Coverage weekly limit counter | `clinica:{tenantId}:coverage:{patientId}:{isoWeek}` | 1 hour | Decremented on appointment booking |
| No-show risk score cache | `clinica:{tenantId}:noshow:{appointmentId}` | 30 min | Recalculated on appointment update |
| JWT revocation list | `clinica:jti:{jti}` | = token remaining TTL | Set on logout or token rotation |
| Notification deduplication | `clinica:{tenantId}:notif:{appointmentId}:{channel}` | 48 hours | Prevents duplicate SMS/email |
| Login rate limiting | `clinica:ratelimit:login:{ip}` | 60 s | Counter, max 5 attempts/min (T-003); not tenant-scoped — pre-auth |

**Not allowed in Redis:**
- Patient records or any FHIR resource
- Appointment data (PostgreSQL is source of truth)
- User credentials or hashed passwords
- Any data that would cause data loss if Redis restarts

---

## 2. Key Naming Conventions

Format: `clinica:{tenantId}:{domain}:{identifier}[:{qualifier}]`

```
clinica:550e8400-e29b-...:perms:user-uuid-here
clinica:550e8400-e29b-...:coverage:patient-uuid-here:2026-W23
clinica:550e8400-e29b-...:noshow:appointment-uuid-here
clinica:jti:jwt-jti-claim-here
```

Rules:
- Always prefix with `clinica:` — prevents collision with other apps on same Redis
- Always include `tenantId` in tenant-scoped keys — prevents cross-tenant cache poisoning
- Use UUIDs as identifiers, never names or emails
- Keep keys under 100 characters
- Use `:` as separator (Redis convention)

---

## 3. TTL Strategy

- **Every key must have a TTL** — no persistent keys in Redis
- TTLs are defined as constants in `RedisConstants.java`, not scattered in business code:

```java
public final class RedisConstants {
    public static final Duration PERMISSIONS_TTL    = Duration.ofMinutes(5);
    public static final Duration COVERAGE_TTL       = Duration.ofHours(1);
    public static final Duration NOSHOW_SCORE_TTL   = Duration.ofMinutes(30);
    public static final Duration NOTIF_DEDUP_TTL    = Duration.ofHours(48);

    private RedisConstants() {}
}
```

- Never hardcode TTL values inline in service code

---

## 4. Invalidation Strategy

| Trigger | Keys to invalidate |
|---|---|
| Role or permission modified | `clinica:{tenantId}:perms:*` (all users in tenant) |
| Appointment booked / cancelled | `clinica:{tenantId}:coverage:{patientId}:{week}` |
| Appointment data changed | `clinica:{tenantId}:noshow:{appointmentId}` |
| User logout | `clinica:jti:{jti}` (set with remaining TTL) |

Invalidation is performed by the **Application Service** that performs the
write, not by the controller or repository:

```java
// In BookAppointmentUseCaseImpl
appointmentRepository.save(appointment);
cacheService.invalidateCoverageCache(tenantId, patientId, weekKey);
```

---

## 5. Availability and Fallback

Redis is a non-critical dependency. The application must operate (with
degraded performance) when Redis is unavailable:

```java
// RedisCircuitBreaker wraps all Redis calls
try {
    return permissionCache.get(key);
} catch (RedisConnectionFailureException e) {
    log.warn("Redis unavailable, falling back to database: {}", e.getMessage());
    return permissionRepository.findByUser(userId);
}
```

Every Redis call is wrapped in try-catch; exceptions are logged as WARN,
not ERROR. The system falls back to the database source of truth.

---

## 6. Serialization

- Store Java objects as JSON (using Jackson `ObjectMapper`)
- Use `GenericJackson2JsonRedisSerializer` for values
- Use `StringRedisSerializer` for keys
- Never use Java serialization (`JdkSerializationRedisSerializer`) — not portable

---

## 7. Configuration

```yaml
# application.yml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      connect-timeout: 1000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 4
          min-idle: 1
```
