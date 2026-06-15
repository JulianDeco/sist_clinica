# Estándares de Documentación

> Principio: cada decisión, cada clase, cada método público debe estar
> documentado en el momento de su creación — no de forma retroactiva. La
> documentación es un entregable de primer nivel, no una ocurrencia tardía.

---

## 1. Niveles de Documentación

| Nivel | Qué | Dónde |
|---|---|---|
| **Arquitectura** | Por qué se eligió una tecnología o patrón | `docs/adr/` (ADRs) |
| **Spec de feature** | Qué debe hacer una feature antes de comenzar a codificar | `docs/modules/specs/` |
| **Contexto de módulo** | Cómo funciona un módulo, sus contratos y máquina de estados | `docs/modules/` |
| **Nivel de código** | Qué hace una clase o método | JavaDoc en todo elemento público |
| **Registro de decisiones** | Por qué el código se escribió de una manera específica (no obvio) | Comentario inline cuando sea crítico |

---

## 2. Estándares de JavaDoc

Toda clase `public`, interfaz y método en el backend requiere JavaDoc.

### JavaDoc a Nivel de Clase

```java
/**
 * Servicio de aplicación que implementa el caso de uso de reserva de turno (UC-01).
 *
 * <p>Orquesta la verificación de disponibilidad del slot, validación de cobertura
 * de obra social, puntuación de riesgo de ausentismo y creación atómica del turno.
 *
 * <p>Reglas de negocio aplicadas:
 * <ul>
 *   <li>El slot solicitado debe estar en estado {@code FREE}.</li>
 *   <li>El límite semanal de cobertura del paciente no debe superarse.</li>
 *   <li>Si la puntuación de riesgo supera el umbral y el profesional tiene habilitado
 *       el overbooking, se incluye una sugerencia de overbooking en el resultado.</li>
 * </ul>
 *
 * <p>Ver spec: {@code docs/modules/agenda/specs/BookAppointment.spec.md}
 *
 * @see BookAppointmentCommand
 * @see AppointmentResult
 */
@Service
@Transactional
public class BookAppointmentUseCaseImpl implements BookAppointmentUseCase { ... }
```

### JavaDoc a Nivel de Método

```java
/**
 * Ejecuta el caso de uso de reserva de turno.
 *
 * <p>Realiza la reserva del slot, decremento de cobertura y vinculación de la
 * puntuación de riesgo en una única transacción de base de datos. Ante un fallo,
 * la transacción se revierte y no se confirman efectos secundarios.
 *
 * @param command la solicitud de reserva que contiene los identificadores de slot,
 *                paciente y tenant
 * @return el turno creado con su ID FHIR y la evaluación de riesgo
 * @throws SlotAlreadyBookedException  si el slot no está en estado FREE
 * @throws CoverageExceededException   si se alcanzó el límite semanal de cobertura del paciente
 * @throws ResourceNotFoundException   si el slot o el paciente no existen en este tenant
 */
AppointmentResult execute(BookAppointmentCommand command);
```

### Reglas

- No repetir lo que el nombre del método ya dice — documentar el **por qué** y las
  **restricciones**
- Siempre documentar `@throws` para excepciones verificadas y runtime significativas
- Referenciar el archivo de spec y los ADRs relacionados con `@see` o enlace inline
- Evitar Javadoc en métodos privados a menos que la lógica sea no obvia

---

## 3. Documentación de Archivos Spec (Spec-Driven Development)

Cada feature comienza con un archivo de spec antes de cualquier implementación.
Ver [Estándares de Testing §5](../testing/01-testing-standards.md) para el template completo.

Regla clave: el archivo de spec se **confirma antes de la primera línea de código
de implementación**.

---

## 4. Archivos de Documentación de Módulos

Para cada módulo backend, crear `docs/modules/{module}.md`:

```markdown
# Módulo: Agenda

**Estado**: EN DESARROLLO
**Última actualización**: 2026-06-08
**Relaciona con**: UC-01, UC-04

---

## Responsabilidad
Gestiona la reserva de turnos, administración de slots y lógica de overbooking.

## Modelo de Dominio
(Diagrama de clases Mermaid)

## Máquina de Estados
(Transiciones de estado del turno)

## API Pública
(Lista de endpoints)

## Reglas de Negocio
(Lista numerada)

## Dependencias
(Otros módulos que este módulo llama)

## Configuración
(Parámetros configurables)

## Limitaciones Conocidas / TODOs
```

---

## 5. Formato de ADR

Ver `docs/adr/README.md` para el template completo de ADR. Todo ADR es inmutable
una vez fusionado — modificar creando un nuevo ADR que lo supersede.

---

## 6. Política de Comentarios en Código

Los comentarios inline son raros y de alto valor únicamente:

```java
// CORRECTO — explica una restricción no obvia
// La fuerza 12 de BCrypt es deliberadamente mayor que el valor predeterminado (10)
// para aumentar el costo de fuerza bruta. A 300ms/hash esto es aceptable para
// la latencia de login.
return new BCryptPasswordEncoder(12);

// INCORRECTO — reitera lo que el código ya dice
// Crear un nuevo codificador BCrypt con fuerza 12
return new BCryptPasswordEncoder(12);
```

Reglas:
- Comentar el **por qué**, nunca el **qué**
- Usar comentarios TODO para limitaciones conocidas: `// TODO(T-042): agregar rate limiting`
- Nunca dejar código comentado — eliminarlo, el historial de Git es el archivo

---

## 7. El Mensaje de Commit como Documentación

Los mensajes de commit son documentación permanente del proyecto. Seguir Conventional Commits:

```
feat(agenda): implement slot availability check for UC-01

Validates that a requested slot is in FREE status before booking.
Returns SlotAlreadyBookedException (409) if occupied.

Spec: docs/modules/agenda/specs/BookAppointment.spec.md
ADR: docs/adr/ADR-009-appointment-booking-rules.md
```

- Línea de asunto: `{tipo}({alcance}): {imperativo, minúsculas, sin punto}`
- Cuerpo: explicar *por qué* se hizo el cambio si no es obvio desde el título
- Referenciar spec y ADR cuando sea relevante

---

## 8. Workflow de Documentación

```
Nueva feature:
  1. Escribir archivo de spec (docs/modules/{x}/specs/{X}.spec.md)
  2. Obtener aprobación del spec (revisión en PR o OK explícito en chat)
  3. Escribir implementación + tests
  4. Escribir/actualizar doc del módulo (docs/modules/{x}.md)
  5. Escribir ADR si se tomó una decisión significativa
  6. Confirmar todo junto (spec + código + tests + doc del módulo)

Todo en un solo PR — la documentación nunca es un seguimiento posterior.
```

---

## 9. Registro de Decisiones

Toda decisión técnica no obvia tomada durante una sesión se registra en una entrada del
registro de decisiones en `docs/adr/decisions-log.md` (informal, solo adición) antes
de que se escriba un ADR formal. Esto captura el razonamiento mientras está fresco.

Formato:
```markdown
## 2026-06-08 — Se eligió multitenancy por fila en lugar de esquema por tenant

**Contexto**: El VPS tiene 4GB RAM. El esquema por tenant requeriría N connection pools.
**Decisión**: Por fila con columna tenant_id.
**Trade-off**: Todos los tenants comparten tablas — una cláusula WHERE tenant_id faltante
  filtraría datos entre tenants. Mitigado por la clase base TenantAwareRepository.
**Formalizado en**: ADR-003
```
