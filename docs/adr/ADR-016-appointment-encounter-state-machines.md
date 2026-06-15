# ADR-016: Máquinas de estado separadas para Appointment y Encounter

**Estado**: ACCEPTED
**Fecha**: 2026-06-15
**Autor**: JulianDeco
**Relaciona con**: UC-01, UC-02, T-006, T-007

---

## Contexto

El dominio clínico tiene dos recursos FHIR con ciclos de vida propios:
`Appointment` (turno) y `Encounter` (consulta médica). Una decisión temprana
(2026-06-10, registrada en `use-cases.md`) estableció máquinas de estado
separadas para cada recurso. Este ADR documenta esa decisión formalmente,
define los estados permitidos y las transiciones válidas, y establece las
reglas de integridad referencial entre ambos ciclos.

El diseño impacta directamente las entidades JPA (T-006/T-007), las
migraciones Flyway, los tests de dominio, y los diagramas ICONIX de CU-01
y CU-02.

## Problema

¿Deben Appointment y Encounter compartir una única máquina de estados o
tener ciclos de vida independientes? La respuesta afecta el modelo de datos,
la lógica de transición y la complejidad de la API.

## Opciones Consideradas

| Option | Summary |
|---|---|
| A — Estado único compartido | Un campo `status` en Appointment que refleja también el estado de la consulta |
| B — Máquinas de estado separadas | Appointment y Encounter tienen campos `status` propios con transiciones independientes |
| C — Encounter hereda estado de Appointment | Encounter no tiene estado propio; su estado se deriva del Appointment |

## Decisión

**Opción B — máquinas de estado separadas**, consistente con FHIR R4
(`Appointment.status` y `Encounter.status` son recursos independientes con
valuesets propios).

### Appointment — estados y transiciones

```
DRAFT ──► BOOKED ──► ARRIVED ──► FULFILLED
                  │           └──► NOSHOW
                  └──► CANCELLED
```

| Transición | Disparador |
|---|---|
| `DRAFT → BOOKED` | Secretario confirma la reserva (CU-01) |
| `BOOKED → ARRIVED` | Paciente llega; secretario registra llegada |
| `BOOKED → CANCELLED` | Cancelación por cualquier actor antes de la llegada |
| `ARRIVED → FULFILLED` | Encounter se cierra como finalizado (CU-02) |
| `ARRIVED → NOSHOW` | Job nocturno detecta turno sin Encounter asociado al día siguiente |

### Encounter — estados y transiciones

```
PLANNED ──► IN_PROGRESS ──► FINISHED
                         └──► CANCELLED
```

| Transición | Disparador |
|---|---|
| `PLANNED → IN_PROGRESS` | Médico abre la consulta (vinculada a Appointment en `ARRIVED`) |
| `IN_PROGRESS → FINISHED` | Médico cierra el Encounter (CU-02, paso 8) |
| `IN_PROGRESS → CANCELLED` | Médico abandona y cancela explícitamente |

### Reglas de integridad referencial

1. Un `Encounter` solo puede crearse si el `Appointment` asociado está en
   estado `ARRIVED` (o es un walk-in que genera el Appointment retroactivo en `ARRIVED`).
2. El cierre del `Encounter` (`IN_PROGRESS → FINISHED`) dispara
   atómicamente la transición `ARRIVED → FULFILLED` del `Appointment`.
3. Un `Appointment` no puede pasar a `FULFILLED` sin un `Encounter`
   en estado `FINISHED` asociado.
4. Walk-in: se permite crear `Encounter` sin `Appointment` previo. En ese
   caso el sistema crea el `Appointment` retroactivo en estado `ARRIVED`
   antes de crear el `Encounter`. La transacción es atómica.

### Turno sin cobertura (UC-01, decisión 2026-06-10)

Un `Appointment` puede crearse con la asociación `Appointment—Coverage`
vacía (`0..1`). No tiene estado especial — el flag se resuelve al imputar
a obra social en el momento del Encounter.

## Consecuencias

**Positive:**
- Alineamiento con FHIR R4 (valuesets `appointment-status` y
  `encounter-status` son distintos en la spec).
- Cada recurso puede evolucionar su ciclo de vida independientemente.
- Las reglas de integridad son explícitas y testeables con pruebas de
  dominio (state machine tests).
- Los diagramas ICONIX de CU-01 y CU-02 reflejan fielmente el dominio.

**Negative:**
- Más lógica en el Application Service de CU-02: debe hacer dos
  transiciones atómicas (Encounter + Appointment) en un solo
  `@Transactional`.
- Mayor superficie de test: se necesitan tests para ambas máquinas y
  para la invariante de cierre atómico.

## Compromisos

- Se acepta la complejidad adicional en el use case de cierre a cambio de
  fidelidad al estándar FHIR y separación de responsabilidades.
- Si en el futuro los requisitos de auditoría exigen una línea de tiempo
  unificada, se puede agregar una proyección de lectura (CQRS read model)
  sin cambiar el dominio.

## Notas

- Los estados de `Appointment` mapean al valueset FHIR
  `http://hl7.org/fhir/appointmentstatus`.
- Los estados de `Encounter` mapean al valueset FHIR
  `http://hl7.org/fhir/encounter-status`.
- El job que detecta NOSHOW (CU-03) opera sobre Appointments en `ARRIVED`
  sin Encounter `FINISHED` al día siguiente.
- Implementación en: `domain/appointment/AppointmentStatus.java`,
  `domain/encounter/EncounterStatus.java` (T-006 y T-007 respectivamente).
- Relacionado: ADR-009 (FHIR JSONB storage), ADR-010 (Clean Architecture).
