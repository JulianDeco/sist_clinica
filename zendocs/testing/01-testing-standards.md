# Testing Standards — Kuris

Stack: JUnit 5 · Mockito · Spring Boot Test · Testcontainers · AssertJ
       Jasmine · Karma · Angular Testing Library (frontend)

---

## 1. Testing Philosophy — TDD Explicit Cycle

Every feature follows **Test-Driven Development (TDD)**. This is not optional
and it is not "write tests after" — the test must exist and fail before the
first line of production code is written.

```
RED     Write a test that references a class or method that does not exist yet.
        Run mvn test / ng test. Required outcome: compilation error or failure.
        If it passes → the test is wrong or already covered. Fix it.

GREEN   Write the minimum production code to make the test pass.
        Nothing more. No "while I'm here" additions.
        Run mvn test / ng test. Required outcome: all tests pass.

REFACTOR  Clean structure without changing behavior.
          Extract, rename, simplify. Run tests after every change.
          Required outcome: still GREEN.

COMMIT  Only after the full Red → Green → Refactor cycle is complete.
```

**Non-negotiable rules:**

| Rule | Detail |
|---|---|
| Test before class | The production class must not exist when you write the test |
| Red phase is mandatory | Run the suite and see it fail. Do not skip this. |
| Minimum code in Green | If no test demands it, do not implement it |
| Testcontainers only | No H2, no in-memory mocks for database or Redis |
| Tenant isolation test | Required for every new repository method, without exception |
| Test names are requirements | `method_givenCondition_expectedBehavior` — readable as a spec |

No code is committed without a corresponding test. Tests document behavior,
not implementation details.

---

## 2. Unit Tests (Backend — Java)

### Scope

- **Application services / use cases**: test every business rule in isolation
- **Domain entities**: test state transitions, invariants, validation
- **Value objects**: test equality, validation, factory methods
- No tests for: DTOs, controllers (covered by integration tests), repositories

### Tooling

- JUnit 5 (`@Test`, `@ParameterizedTest`, `@MethodSource`)
- Mockito (`@Mock`, `@InjectMocks`) — mock all collaborators
- AssertJ fluent assertions

### Naming Convention

```java
// Format: methodOrScenario_givenCondition_expectedBehavior
@Test
void bookAppointment_whenSlotIsAlreadyBooked_throwsSlotAlreadyBookedException()

@Test
void bookAppointment_whenPatientHasNoHistory_calculatesScoreWithLowConfidence()

@Test
void cancel_whenAppointmentIsAlreadyFulfilled_throwsDomainException()
```

### Structure (AAA pattern)

```java
@Test
void bookAppointment_whenCoverageExceeded_throwsCoverageExceededException() {
    // Arrange
    UUID tenantId = UUID.randomUUID();
    var command = new BookAppointmentCommand(tenantId, slotId, patientId, null);
    when(coverageService.checkLimit(tenantId, patientId)).thenReturn(LimitStatus.EXCEEDED);

    // Act + Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(CoverageExceededException.class)
        .hasMessageContaining("weekly limit");
}
```

### Coverage Expectations

| Layer | Minimum Coverage |
|---|---|
| Application services (use cases) | 90% line coverage |
| Domain entities | 95% line coverage |
| Value objects | 100% |
| Infrastructure adapters | 70% (integration tests cover the rest) |

---

## 3. Integration Tests (Backend)

### Scope

- **Controllers**: test full HTTP round-trip (request → response + status + body)
- **Repositories**: test actual SQL queries against real PostgreSQL
- **Security filters**: test JWT validation, tenant isolation

### Tooling

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` for full context
- **Testcontainers** for PostgreSQL and Redis — no H2, no mocks for databases
- `MockMvc` or `WebTestClient` for HTTP assertions
- `@Transactional` + rollback on repository tests

```java
// Testcontainers setup — shared across test suite for performance
@Testcontainers
@SpringBootTest
public abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("clinica_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis =
        new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

### Controller Integration Test Pattern

```java
@Test
void bookAppointment_validRequest_returns201WithLocation() throws Exception {
    String jwt = testJwtFactory.forUser(testUser, testTenant);

    mockMvc.perform(post("/api/v1/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + jwt)
            .header("X-Tenant-ID", testTenant.getId().toString())
            .content("""
                {
                    "slotId": "%s",
                    "patientId": "%s"
                }
                """.formatted(testSlot.getId(), testPatient.getId())))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("BOOKED"))
        .andExpect(jsonPath("$.id").isNotEmpty());
}
```

### Tenant Isolation Test (mandatory for every repository)

```java
@Test
void findByTenantId_onlyReturnsTenantOwnedRecords() {
    var tenant1 = createTenant();
    var tenant2 = createTenant();
    createAppointment(tenant1);
    createAppointment(tenant2);

    List<Appointment> results = repository.findByTenantId(tenant1.getId(), Pageable.unpaged());

    assertThat(results).hasSize(1)
        .allMatch(a -> a.getTenantId().equals(tenant1.getId()));
}
```

---

## 4. Frontend Tests (Angular)

### Tooling

- **Jasmine + Karma** for unit and component tests
- **Angular Testing Library** for behavior-focused component tests
- **HttpClientTestingModule** for service tests

### Component Test Pattern

```typescript
// agenda-calendar.component.spec.ts
describe('AgendaCalendarComponent', () => {
    let component: AgendaCalendarComponent;
    let fixture: ComponentFixture<AgendaCalendarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [AgendaCalendarComponent, HttpClientTestingModule]
        }).compileComponents();
        fixture = TestBed.createComponent(AgendaCalendarComponent);
        component = fixture.componentInstance;
    });

    it('should display appointments for selected date', () => {
        component.appointments = signal([mockAppointment]);
        fixture.detectChanges();
        const cards = fixture.nativeElement.querySelectorAll('[data-testid="appointment-card"]');
        expect(cards.length).toBe(1);
    });

    it('should emit dateChange when user selects a new date', () => {
        spyOn(component.dateChange, 'emit');
        const dateButton = fixture.nativeElement.querySelector('[data-testid="date-2026-06-10"]');
        dateButton.click();
        expect(component.dateChange.emit).toHaveBeenCalledWith(new Date('2026-06-10'));
    });
});
```

### Service Test Pattern

```typescript
// appointments.api.spec.ts
describe('AppointmentsApi', () => {
    let service: AppointmentsApi;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
        service = TestBed.inject(AppointmentsApi);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => httpMock.verify());

    it('should POST to /api/v1/appointments with correct body', () => {
        service.book(mockRequest).subscribe();
        const req = httpMock.expectOne('/api/v1/appointments');
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(mockRequest);
    });
});
```

### Frontend Coverage Expectations

| Area | Minimum Coverage |
|---|---|
| Stores (state management) | 90% |
| API client services | 80% |
| Guards and interceptors | 100% |
| Presentational components | 70% |

---

## 5. Spec-Driven Development (SDD)

Every feature begins with a **specification file** before any implementation.
The spec is the contract between requirements, tests, and code.

### Spec File Location

```
docs/modules/{module-name}/specs/{FeatureName}.spec.md
```

### Spec File Structure

```markdown
# Spec: {Feature Name}

**Status**: DRAFT | APPROVED | IMPLEMENTED
**Author**: {name}
**Date**: {ISO date}
**Relates to**: {UC or task ID}

---

## 1. Goal
One paragraph describing what this feature does and why.

## 2. Inputs
List every input: HTTP method, path, headers, body fields with types and constraints.

## 3. Business Rules
Numbered list of every rule that must be enforced:
- BR-01: Slot must not already be booked.
- BR-02: Patient coverage weekly limit must not be exceeded.

## 4. Outputs
- Success case: status code, response body shape
- Each error case: status code, errorCode, trigger condition

## 5. Test Cases
For each business rule, list at least one test case:
- TC-01 (BR-01): Given a booked slot, expect 409 SLOT_ALREADY_BOOKED
- TC-02 (BR-02): Given exceeded coverage, expect 409 COVERAGE_LIMIT_EXCEEDED
- TC-03 (happy path): Given valid inputs, expect 201 Created with appointment

## 6. ADR References
Link to any ADR relevant to this feature.

## 7. Open Questions
Unresolved decisions that need answers before implementation starts.
```

### SDD Workflow

```
1. Write spec file (BA / developer)
2. Review and APPROVE spec (team / owner)
3. Write test stubs from spec test cases (TDD)
4. Implement until all tests pass
5. Update spec status → IMPLEMENTED
6. Commit spec file alongside implementation
```

**No implementation starts without an APPROVED spec.** If something is not
in the spec, it is out of scope for this feature.

---

## 6. Test Data Factories

Avoid constructing test data inline. Use builder-pattern factories:

```java
// test/factory/AppointmentTestFactory.java
public class AppointmentTestFactory {
    public static Appointment validBooked(UUID tenantId) {
        return Appointment.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .status(AppointmentStatus.BOOKED)
            .date(LocalDate.now().plusDays(1))
            .build();
    }
}
```

---

## 7. Minimum Acceptable Coverage

Overall project:
- **Backend**: 80% line coverage (enforced by JaCoCo Maven plugin — build fails below threshold)
- **Frontend**: 75% statement coverage (enforced by Karma coverage reporter)

These are minimums, not targets. Aim for coverage driven by meaningful test
cases, not by inflating numbers with trivial assertions.
