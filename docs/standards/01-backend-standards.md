# Backend Standards — Java 21 + Spring Boot 3

---

## 1. Package Structure

```
com.clinicasaas
├── config/                         # @Configuration, @Bean definitions
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── JpaConfig.java
│   ├── FlywayConfig.java
│   └── OpenApiConfig.java
│
├── domain/                         # Pure domain: entities, value objects, enums
│   ├── shared/                     # Cross-domain: TenantId, AuditFields, etc.
│   ├── user/
│   │   ├── User.java               # JPA Entity
│   │   ├── UserRepository.java     # Interface (Spring Data JPA)
│   │   └── Role.java               # Enum or Entity
│   ├── patient/
│   ├── appointment/
│   ├── encounter/
│   └── fhir/
│
├── application/                    # Use cases — no framework dependencies here
│   ├── auth/
│   │   ├── LoginUseCase.java       # Interface
│   │   ├── LoginUseCaseImpl.java   # @Service implementation
│   │   ├── command/
│   │   │   └── LoginCommand.java   # Input record/DTO
│   │   └── result/
│   │       └── TokenResult.java    # Output record
│   ├── agenda/
│   └── clinical/
│
├── infrastructure/                 # Adapters to external systems
│   ├── persistence/                # JPA repository implementations, Specifications
│   ├── cache/                      # Redis adapter implementations
│   ├── notifications/              # Email, WhatsApp, SMS clients
│   └── security/                   # JWT provider, password encoder
│
└── api/                            # HTTP layer — controllers and DTOs
    ├── v1/
    │   ├── auth/
    │   │   ├── AuthController.java
    │   │   └── dto/
    │   │       ├── LoginRequest.java
    │   │       └── TokenResponse.java
    │   ├── appointments/
    │   └── patients/
    ├── fhir/                       # FHIR R4 endpoints
    └── exception/
        ├── GlobalExceptionHandler.java
        └── ErrorResponse.java
```

---

## 2. Layer Responsibilities

### Controllers (`api/`)

**Allowed:**
- Parse and validate HTTP requests (via `@Valid`)
- Map request DTOs to application commands
- Call exactly one application service / use case
- Map application results to response DTOs
- Return `ResponseEntity<T>` with explicit HTTP status

**Forbidden:**
- Business logic of any kind
- Direct repository access
- Catching exceptions (delegate to `GlobalExceptionHandler`)
- Constructing domain entities

```java
// CORRECT
@PostMapping
public ResponseEntity<AppointmentResponse> book(
        @Valid @RequestBody BookAppointmentRequest request) {
    AppointmentResult result = bookAppointmentUseCase.execute(
            BookAppointmentCommand.from(request, tenantContext.getTenantId()));
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(AppointmentResponse.from(result));
}

// WRONG — business logic in controller
@PostMapping
public ResponseEntity<?> book(@RequestBody BookAppointmentRequest request) {
    if (request.getDate().isBefore(LocalDate.now())) { // ← belongs in domain
        return ResponseEntity.badRequest().build();
    }
    ...
}
```

---

### Application Services (`application/`)

**Allowed:**
- Orchestrate domain objects and repositories
- Enforce business rules and invariants
- Manage transactions (`@Transactional`)
- Publish domain events
- Call infrastructure ports (cache, notifications, external services)

**Forbidden:**
- HTTP concepts (no `HttpServletRequest`, no Spring MVC annotations)
- Direct JPA `EntityManager` calls (use repository interfaces)
- Returning JPA entities — always return result records/DTOs

```java
// CORRECT
@Service
@Transactional
public class BookAppointmentUseCaseImpl implements BookAppointmentUseCase {

    @Override
    public AppointmentResult execute(BookAppointmentCommand command) {
        Slot slot = slotRepository.findByIdAndTenantOrThrow(
                command.slotId(), command.tenantId());
        slot.reserve(); // domain logic inside entity
        appointmentRepository.save(appointment);
        return AppointmentResult.from(appointment);
    }
}
```

---

### Domain Entities (`domain/`)

**Allowed:**
- JPA annotations (`@Entity`, `@Column`, etc.)
- Business behavior methods that enforce invariants
- Value objects as `@Embeddable`
- Domain state transitions

**Forbidden:**
- Spring `@Autowired` (entities must not depend on Spring beans)
- Direct database calls
- HTTP or serialization concerns

```java
@Entity
@Table(name = "appointments")
public class Appointment {

    // state transition — business logic lives here, not in service
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

### Repositories (`domain/*/XxxRepository.java`)

- Define as interfaces extending `JpaRepository<Entity, ID>`
- Custom finders use **Spring Data derived queries** for simple cases
- Complex queries use `@Query` (JPQL preferred over native SQL)
- Multitenancy filter applied via `TenantAwareRepository` base interface:

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

## 3. DTO Strategy

- **Request DTOs**: Java `record` classes with `@Valid` and Bean Validation annotations
- **Response DTOs**: Java `record` classes with static factory `from(Domain)` method
- **Command objects**: `record` in `application/*/command/` — bridge between API and application
- **Result objects**: `record` in `application/*/result/` — bridge between application and API
- DTOs never cross the application boundary (entities stay inside)

```java
// Request DTO
public record BookAppointmentRequest(
    @NotNull UUID slotId,
    @NotNull UUID patientId,
    @Size(max = 500) String notes
) {}

// Response DTO
public record AppointmentResponse(UUID id, String status, LocalDateTime date) {
    public static AppointmentResponse from(AppointmentResult result) {
        return new AppointmentResponse(result.id(), result.status(), result.date());
    }
}
```

---

## 4. Exception Handling Strategy

Define a hierarchy in `api/exception/`:

```java
// Base — all business exceptions extend this
public abstract class ClinicaSaasException extends RuntimeException {
    public abstract HttpStatus httpStatus();
    public abstract String errorCode();   // e.g. "SLOT_ALREADY_BOOKED"
}

// Concrete exceptions
public class SlotAlreadyBookedException extends ClinicaSaasException {
    @Override public HttpStatus httpStatus() { return HttpStatus.CONFLICT; }
    @Override public String errorCode() { return "SLOT_ALREADY_BOOKED"; }
}

public class ResourceNotFoundException extends ClinicaSaasException {
    @Override public HttpStatus httpStatus() { return HttpStatus.NOT_FOUND; }
    @Override public String errorCode() { return "RESOURCE_NOT_FOUND"; }
}

public class TenantAccessDeniedException extends ClinicaSaasException {
    @Override public HttpStatus httpStatus() { return HttpStatus.FORBIDDEN; }
    @Override public String errorCode() { return "TENANT_ACCESS_DENIED"; }
}
```

`GlobalExceptionHandler` maps to `ErrorResponse`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClinicaSaasException.class)
    public ResponseEntity<ErrorResponse> handle(ClinicaSaasException ex) {
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

## 5. Validation Strategy

- All input validated at the HTTP boundary using **Jakarta Bean Validation**
- No manual `if (x == null)` checks in controllers or services for input
- Domain-level validation (business rules) lives in domain entities and throws domain exceptions
- Use `@Validated` on service classes for method-level parameter validation when needed

Annotations used:
```
@NotNull, @NotBlank, @Size, @Email, @Min, @Max, @Pattern, @Future, @PastOrPresent
```

---

## 6. Logging Strategy

- Use **SLF4J** with Logback (Spring Boot default)
- Logger as static final field: `private static final Logger log = LoggerFactory.getLogger(X.class)`
- **Do not log passwords, tokens, or PII**
- Log levels:
  - `ERROR`: unrecoverable errors, unexpected states
  - `WARN`: recoverable issues, degraded operation (e.g. cache miss)
  - `INFO`: business events (appointment created, user authenticated)
  - `DEBUG`: internal flow, only in development

```java
// CORRECT
log.info("Appointment created: id={}, tenant={}, patient={}", 
         appointment.getId(), tenantId, patientId);

// WRONG — logs sensitive data
log.debug("User login attempt: email={}, password={}", email, password);
```

---

## 7. Multitenancy Pattern

Every request carries `tenant_id` from the JWT claim.
`TenantContextHolder` (ThreadLocal) is populated by `TenantContextFilter`.

```java
// Filter populates ThreadLocal
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

// Repository usage — ALWAYS pass tenantId explicitly
List<Appointment> findByTenantIdAndDate(UUID tenantId, LocalDate date);
```

**Rule**: Every repository query that touches tenant data **must** include
`tenantId` as a parameter. No exceptions.
