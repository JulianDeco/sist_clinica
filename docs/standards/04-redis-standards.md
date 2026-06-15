# Estándares de Redis — Redis 8

---

## 1. Casos de Uso Permitidos

Redis es **solo caché y almacén efímero** — nunca almacenamiento primario.

| Caso de Uso | Patrón de Clave | TTL | Notas |
|---|---|---|---|
| Caché de permisos RBAC | `clinica:{tenantId}:perms:{userId}` | 5 min | Invalidada ante cambio de rol/permiso |
| Contador semanal de cobertura | `clinica:{tenantId}:coverage:{patientId}:{isoWeek}` | 1 hora | Decrementado al reservar turno |
| Caché de puntuación de riesgo de ausentismo | `clinica:{tenantId}:noshow:{appointmentId}` | 30 min | Recalculada al actualizar el turno |
| Lista de revocación JWT | `clinica:jti:{jti}` | = TTL restante del token | Establecida al cerrar sesión o rotar token |
| Deduplicación de notificaciones | `clinica:{tenantId}:notif:{appointmentId}:{channel}` | 48 horas | Previene SMS/email duplicados |
| Rate limiting de login | `clinica:ratelimit:login:{ip}` | 60 s | Contador, máx. 5 intentos/min (T-003); sin alcance de tenant — pre-auth |

**No permitido en Redis:**
- Registros de pacientes ni ningún recurso FHIR
- Datos de turnos (PostgreSQL es la fuente de verdad)
- Credenciales de usuario o contraseñas con hash
- Cualquier dato cuya pérdida sería problemática si Redis se reinicia

---

## 2. Convenciones de Nomenclatura de Claves

Formato: `clinica:{tenantId}:{dominio}:{identificador}[:{calificador}]`

```
clinica:550e8400-e29b-...:perms:user-uuid-here
clinica:550e8400-e29b-...:coverage:patient-uuid-here:2026-W23
clinica:550e8400-e29b-...:noshow:appointment-uuid-here
clinica:jti:jwt-jti-claim-here
```

Reglas:
- Siempre prefijar con `clinica:` — previene colisiones con otras apps en el mismo Redis
- Siempre incluir `tenantId` en claves con alcance de tenant — previene envenenamiento
  de caché entre tenants
- Usar UUIDs como identificadores, nunca nombres ni emails
- Mantener las claves en menos de 100 caracteres
- Usar `:` como separador (convención de Redis)

---

## 3. Estrategia de TTL

- **Toda clave debe tener un TTL** — sin claves persistentes en Redis
- Los TTLs se definen como constantes en `RedisConstants.java`, no dispersos en el
  código de negocio:

```java
public final class RedisConstants {
    public static final Duration PERMISSIONS_TTL    = Duration.ofMinutes(5);
    public static final Duration COVERAGE_TTL       = Duration.ofHours(1);
    public static final Duration NOSHOW_SCORE_TTL   = Duration.ofMinutes(30);
    public static final Duration NOTIF_DEDUP_TTL    = Duration.ofHours(48);

    private RedisConstants() {}
}
```

- Nunca codificar valores de TTL directamente en el código de servicio

---

## 4. Estrategia de Invalidación

| Disparador | Claves a invalidar |
|---|---|
| Rol o permiso modificado | `clinica:{tenantId}:perms:*` (todos los usuarios del tenant) |
| Turno reservado / cancelado | `clinica:{tenantId}:coverage:{patientId}:{semana}` |
| Datos del turno modificados | `clinica:{tenantId}:noshow:{appointmentId}` |
| Cierre de sesión del usuario | `clinica:jti:{jti}` (establecido con TTL restante) |

La invalidación la realiza el **Servicio de Aplicación** que ejecuta la escritura,
no el controlador ni el repositorio:

```java
// En BookAppointmentUseCaseImpl
appointmentRepository.save(appointment);
cacheService.invalidateCoverageCache(tenantId, patientId, weekKey);
```

---

## 5. Disponibilidad y Fallback

Redis es una dependencia no crítica. La aplicación debe operar (con rendimiento
degradado) cuando Redis no está disponible:

```java
// RedisCircuitBreaker envuelve todas las llamadas a Redis
try {
    return permissionCache.get(key);
} catch (RedisConnectionFailureException e) {
    log.warn("Redis unavailable, falling back to database: {}", e.getMessage());
    return permissionRepository.findByUser(userId);
}
```

Toda llamada a Redis está envuelta en try-catch; las excepciones se registran como
WARN, no como ERROR. El sistema recurre a la fuente de verdad de la base de datos.

---

## 6. Serialización

- Almacenar objetos Java como JSON (usando Jackson `ObjectMapper`)
- Usar `GenericJackson2JsonRedisSerializer` para valores
- Usar `StringRedisSerializer` para claves
- Nunca usar serialización Java (`JdkSerializationRedisSerializer`) — no es portable

---

## 7. Configuración

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
