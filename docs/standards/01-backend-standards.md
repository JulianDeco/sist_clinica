# Estándares de Backend — Java 21 + Spring Boot 3

---

## 1. Estructura de Paquetes

```
com.kuris
├── config/                         # @Configuration, definiciones de @Bean
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── JpaConfig.java
│   ├── FlywayConfig.java
│   └── OpenApiConfig.java
│
├── domain/                         # Dominio puro: entidades, value objects, enums
│   ├── shared/                     # Dominio transversal: TenantId, AuditFields, etc.
│   ├── user/
│   │   ├── User.java               # Entidad JPA
│   │   ├── UserRepository.java     # Interfaz (Spring Data JPA)
│   │   └── Role.java               # Enum o Entidad
│   ├── patient/
│   ├── appointment/
│   ├── encounter/
│   └── fhir/
│
├── application/                    # Casos de uso — sin dependencias de framework aquí
│   ├── auth/
│   │   ├── LoginUseCase.java       # Interfaz
│   │   ├── LoginUseCaseImpl.java   # Implementación @Service
│   │   ├── command/
│   │   │   └── LoginCommand.java   # Record/DTO de entrada
│   │   └── result/
│   │       └── TokenResult.java    # Record de salida
│   ├── agenda/
│   └── clinical/
│
├── infrastructure/                 # Adaptadores a sistemas externos
│   ├── persistence/                # Implementaciones de repositorio JPA, Specifications
│   ├── cache/                      # Implementaciones de adaptadores Redis
│   ├── notifications/              # Clientes de email, WhatsApp, SMS
│   └── security/                   # Proveedor JWT, codificador de contraseñas
│
└── api/                            # Capa HTTP — controladores y DTOs
    ├── v1/
    │   ├── auth/
    │   │   ├── AuthController.java
    │   │   └── dto/
    │   │       ├── LoginRequest.java
    │   │       └── TokenResponse.java
    │   ├── appointments/
    │   └── patients/
    ├── fhir/                       # Endpoints FHIR R4
    └── exception/
        ├── GlobalExceptionHandler.java
        └── ErrorResponse.java
```

---

## 2. Responsabilidades por Capa

### Controladores (`api/`)

**Permitido:**
- Parsear y validar solicitudes HTTP (mediante `@Valid`)
- Mapear DTOs de solicitud a comandos de aplicación
- Llamar exactamente a un servicio de aplicación / caso de uso
- Mapear resultados de aplicación a DTOs de respuesta
- Retornar `ResponseEntity<T>` con código de estado HTTP explícito

**Prohibido:**
- Lógica de negocio de cualquier tipo
- Acceso directo a repositorios
- Capturar excepciones (delegar a `GlobalExceptionHandler`)
- Construir entidades de dominio

```java
// CORRECTO
@PostMapping
public ResponseEntity<AppointmentResponse> book(
        @Valid @RequestBody BookAppointmentRequest request) {
    AppointmentResult result = bookAppointmentUseCase.execute(
            BookAppointmentCommand.from(request, tenantContext.getTenantId()));
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(AppointmentResponse.from(result));
}

// INCORRECTO — lógica de negocio en el controlador
@PostMapping
public ResponseEntity<?> book(@RequestBody BookAppointmentRequest request) {
    if (request.getDate().isBefore(LocalDate.now())) { // ← pertenece al dominio
        return ResponseEntity.badRequest().build();
    }
    ...
}
```

---

### Servicios de Aplicación (`application/`)

**Permitido:**
- Orquestar objetos de dominio y repositorios
- Aplicar reglas de negocio e invariantes
- Gestionar transacciones (`@Transactional`)
- Publicar eventos de dominio
- Llamar a puertos de infraestructura (caché, notificaciones, servicios externos)

**Prohibido:**
- Conceptos HTTP (sin `HttpServletRequest`, sin anotaciones de Spring MVC)
- Llamadas directas a `EntityManager` de JPA (usar interfaces de repositorio)
- Retornar entidades JPA — siempre retornar records/DTOs de resultado

```java
// CORRECTO
@Service
@Transactional
public class BookAppointmentUseCaseImpl implements BookAppointmentUseCase {

    @Override
    public AppointmentResult execute(BookAppointmentCommand command) {
        Slot slot = slotRepository.findByIdAndTenantOrThrow(
                command.slotId(), command.tenantId());
        slot.reserve(); // lógica de dominio dentro de la entidad
        appointmentRepository.save(appointment);
        return AppointmentResult.from(appointment);
    }
}
```

---

### Entidades de Dominio (`domain/`)

**Permitido:**
- Anotaciones JPA (`@Entity`, `@Column`, etc.)
- Métodos de comportamiento de negocio que aplican invariantes
- Value objects como `@Embeddable`
- Transiciones de estado del dominio

**Prohibido:**
- `@Autowired` de Spring (las entidades no deben depender de beans de Spring)
- Llamadas directas a la base de datos
- Preocupaciones HTTP o de serialización

```java
@Entity
@Table(name = "appointments")
public class Appointment {

    // transición de estado — la lógica de negocio vive aquí, no en el servicio
    public void cancel(String reason) {
        if (this.status == AppointmentStatus.FULFILLED) {
            throw new AppointmentAlreadyFulfilledException(this.id);
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
    }
}
```

---

### Repositorios (`domain/*/XxxRepository.java`)

- Definir como interfaces que extienden `JpaRepository<Entity, ID>`
- Los finders personalizados usan **consultas derivadas de Spring Data** para casos simples
- Las consultas complejas usan `@Query` (se prefiere JPQL sobre SQL nativo)
- El filtro de multitenancy se aplica mediante la interfaz base `TenantAwareRepository`:

```java
public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID>, TenantScoped<Appointment> {

    List<Appointment> findByTenantIdAndDateBetween(
            UUID tenantId, LocalDate from, LocalDate to);

    @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
           "AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> findByPatientAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status);
}
```

---

## 3. Estrategia de DTOs

- **DTOs de solicitud**: clases Java `record` con anotaciones `@Valid` y Bean Validation
- **DTOs de respuesta**: clases Java `record` con método factory estático `from(Domain)`
- **Objetos de comando**: `record` en `application/*/command/` — puente entre API y aplicación
- **Objetos de resultado**: `record` en `application/*/result/` — puente entre aplicación y API
- Los DTOs nunca cruzan el límite de la aplicación (las entidades permanecen dentro)

```java
// DTO de solicitud
public record BookAppointmentRequest(
    @NotNull UUID slotId,
    @NotNull UUID patientId,
    @Size(max = 500) String notes
) {}

// DTO de respuesta
public record AppointmentResponse(UUID id, String status, LocalDateTime date) {
    public static AppointmentResponse from(AppointmentResult result) {
        return new AppointmentResponse(result.id(), result.status(), result.date());
    }
}
```

---

## 4. Estrategia de Manejo de Excepciones

Definir una jerarquía en `api/exception/`:

```java
// Base — todas las excepciones de negocio extienden esta
public abstract class KurisException extends RuntimeException {
    public abstract HttpStatus httpStatus();
    public abstract String errorCode();   // por ejemplo "SLOT_ALREADY_BOOKED"
}

// Excepciones concretas
public class SlotAlreadyBookedException extends KurisException {
    @Override public HttpStatus httpStatus() { return HttpStatus.CONFLICT; }
    @Override public String errorCode() { return "SLOT_ALREADY_BOOKED"; }
}

public class ResourceNotFoundException extends KurisException {
    @Override public HttpStatus httpStatus() { return HttpStatus.NOT_FOUND; }
    @Override public String errorCode() { return "RESOURCE_NOT_FOUND"; }
}

public class TenantAccessDeniedException extends KurisException {
    @Override public HttpStatus httpStatus() { return HttpStatus.FORBIDDEN; }
    @Override public String errorCode() { return "TENANT_ACCESS_DENIED"; }
}
```

`GlobalExceptionHandler` mapea a `ErrorResponse`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KurisException.class)
    public ResponseEntity<ErrorResponse> handle(KurisException ex) {
        return ResponseEntity.status(ex.httpStatus())
                .body(ErrorResponse.of(ex.errorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::from).toList();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.validation(errors));
    }
}
```

---

## 5. Estrategia de Validación

- Toda entrada validada en el límite HTTP usando **Jakarta Bean Validation**
- Sin verificaciones manuales `if (x == null)` en controladores o servicios para la entrada
- La validación a nivel de dominio (reglas de negocio) vive en las entidades de dominio
  y lanza excepciones de dominio
- Usar `@Validated` en clases de servicio para validación de parámetros a nivel de método
  cuando sea necesario

Anotaciones usadas:
```
@NotNull, @NotBlank, @Size, @Email, @Min, @Max, @Pattern, @Future, @PastOrPresent
```

---

## 6. Estrategia de Logging

- Usar **SLF4J** con Logback (predeterminado de Spring Boot)
- Logger como campo static final: `private static final Logger log = LoggerFactory.getLogger(X.class)`
- **No registrar contraseñas, tokens ni PII**
- Niveles de log:
  - `ERROR`: errores irrecuperables, estados inesperados
  - `WARN`: problemas recuperables, operación degradada (por ejemplo, cache miss)
  - `INFO`: eventos de negocio (turno creado, usuario autenticado)
  - `DEBUG`: flujo interno, solo en desarrollo

```java
// CORRECTO
log.info("Appointment created: id={}, tenant={}, patient={}", 
         appointment.getId(), tenantId, patientId);

// INCORRECTO — registra datos sensibles
log.debug("User login attempt: email={}, password={}", email, password);
```

---

## 7. Patrón de Multitenancy

Cada solicitud lleva `tenant_id` del claim del JWT.
`TenantContextHolder` (ThreadLocal) es poblado por `TenantContextFilter`.

```java
// El filtro popula el ThreadLocal
@Component
public class TenantContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) throws ... {
        String tenantId = extractFromJwt(request);
        TenantContextHolder.set(UUID.fromString(tenantId));
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}

// Uso en repositorio — SIEMPRE pasar tenantId explícitamente
List<Appointment> findByTenantIdAndDate(UUID tenantId, LocalDate date);
```

**Regla**: Toda consulta de repositorio que toque datos del tenant **debe** incluir
`tenantId` como parámetro. Sin excepciones.
