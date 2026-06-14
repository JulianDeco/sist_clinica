# Casos de uso ICONIX — ClinicaSaaS

> Los 4 casos de uso definidos en este archivo son la unidad académica
> central del Seminario de Trabajo Final (mayo–diciembre 2026).
> Cada UC requiere los diagramas ICONIX: dominio, casos de uso,
> robustez, secuencia, clases.
>
> Decisión de scope (12/05/2026): el MVP del seminario incluye los 4 CU
> de este archivo. UC históricamente listados como "trabajo final año
> próximo" (alta de tenant, historial clínico SOAP avanzado con CIE-10,
> resumen LLM) quedan como roadmap post-MVP — ver `plan-de-trabajo.md`.

---

## UC-01: Reservar turno con validación integral

**Actor principal**: Secretario/a (o profesional si es consultorio
unipersonal).

**Flujo principal**:
1. El usuario selecciona paciente, profesional y franja horaria.
2. El sistema valida disponibilidad del slot (Schedule + Slot FHIR) y
   ausencia de conflictos.
3. El sistema valida tope semanal de la cobertura del paciente
   (Coverage) — descuenta y verifica que no supere el límite.
4. El sistema calcula el riesgo de no-show con el motor heurístico:
   recibe historial del paciente, anticipación, día y franja, devuelve
   score (0–100) + lista de factores influyentes en lenguaje natural.
5. Si el slot tiene riesgo alto (>70%) y el profesional tiene
   overbooking habilitado, el sistema sugiere ofrecer sobreturno en
   simultáneo.
6. El usuario confirma; el sistema crea el `Appointment` FHIR, actualiza
   el Slot a `busy`, y registra el score de riesgo asociado.

**Flujos alternativos / excepciones**:
- Slot ya ocupado → sugerir slots cercanos.
- Cobertura vencida o tope superado → bloquear la imputación a la
  cobertura y advertir a la secretaria; el turno puede crearse igual
  sin imputación a cobertura, sin clasificación especial — asociación
  Turno—Cobertura vacía, 0..1 (decisión 2026-06-10).
- Paciente no tiene historial → score se calcula con factores parciales
  y se marca como "baja confianza".
- Falla del motor heurístico → degradación elegante: turno se crea sin
  score, registrado para reintento.

**Por qué no es CRUD**: combina 3 validaciones independientes
(disponibilidad, cobertura, riesgo) + lógica condicional (overbooking)
+ múltiples actores (sistema, motor heurístico, base de datos). El
estado del Slot, Appointment y contador de cobertura se modifican
atómicamente.

**Recursos FHIR involucrados**: `Patient`, `Practitioner`, `Schedule`,
`Slot`, `Appointment`, `Coverage`.

**Relaciones UML** (5 `<<include>>` · 6 `<<extend>>`):

| Tipo | Sub-caso | Condición |
|---|---|---|
| `<<include>>` | Validar disponibilidad de slot | — siempre |
| `<<include>>` | Validar cobertura y tope semanal | — siempre |
| `<<include>>` | Calcular riesgo de no-show | — siempre |
| `<<include>>` | Crear Appointment FHIR y actualizar Slot | — siempre |
| `<<include>>` | Registrar score de riesgo en turno | — siempre |
| `<<extend>>` | Sugerir slots alternativos | Slot ya ocupado |
| `<<extend>>` | Advertir cobertura vencida o tope superado | Cobertura inválida |
| `<<extend>>` | Crear turno sin imputación a cobertura | Usuario confirma sin cobertura *(decisión 2026-06-10)* |
| `<<extend>>` | Calcular score con baja confianza | Paciente sin historial |
| `<<extend>>` | Registrar turno sin score para reintento | Falla del motor heurístico |
| `<<extend>>` | CU-04 Sugerir overbooking inteligente | Score >70%, overbooking habilitado, tope no alcanzado |

**Diagramas ICONIX requeridos**: dominio · casos de uso · robustez ·
secuencia · clases.

**Tareas relacionadas**: T-010, T-011, T-012.

---

## UC-02: Gestionar consulta médica (SOAP básico)

**Actor principal**: Médico.

**Flujo principal**:
1. El médico abre el `Encounter` vinculado a un `Appointment` existente
   (turno en estado `llegó`; o retoma una consulta `en curso` —
   decisión 2026-06-10: máquinas de estado separadas por recurso).
2. Registra notas SOAP (Subjetivo, Objetivo, Análisis, Plan) en texto
   estructurado.
3. Registra signos vitales y mediciones como `Observation` FHIR
   asociadas al Encounter (TA, FC, peso, talla, glucemia, etc.).
4. Opcionalmente registra un diagnóstico provisorio como descripción
   libre; el sistema sugiere el código CIE-10 más probable a partir del
   texto libre (ADR-012, T-018 — incorporado al MVP el 2026-06-08) y el
   médico conserva el criterio final.
5. Cierra el Encounter; el sistema actualiza el estado del Appointment
   a `fulfilled` y genera un resumen del encuentro en lenguaje natural
   (LLM — ADR-012, T-017) que el médico valida antes del registro
   definitivo.

> Nota (2026-06-10): los diagramas ICONIX de la entrega del 16/06
> documentan CU-02 sin los pasos LLM (se generaron antes de reflejar
> ADR-012 aquí). Pendiente decidir si se incorporan a los diagramas o
> se documentan como extensión post-entrega.

**Flujos alternativos / excepciones**:
- El médico abandona la consulta sin cerrar → Encounter queda en
  estado `in-progress`, se notifica al médico en su próxima sesión.
- Validación de datos clínicos (rangos imposibles) → advertencia, no
  bloqueo.
- Paciente sin Appointment previo (atención sin turno) → permitir
  crear Encounter "walk-in" que genera el Appointment retroactivo.

**Por qué no es CRUD**: dos máquinas de estado separadas (decisión
2026-06-10) — Turno/`Appointment` (reservado → confirmado → llegó →
cumplido / ausente) y ConsultaMedica/`Encounter` (en curso →
finalizada) —, integridad referencial con Appointment y múltiples
Observations, validaciones clínicas contextuales, persistencia
transaccional.

**Recursos FHIR involucrados**: `Encounter`, `Observation`,
`Appointment`, `Patient`, `Practitioner`.

**Relaciones UML** (5 `<<include>>` · 5 `<<extend>>`):

| Tipo | Sub-caso | Condición |
|---|---|---|
| `<<include>>` | Abrir / reanudar Encounter vinculado | — siempre |
| `<<include>>` | Registrar notas SOAP estructuradas | — siempre |
| `<<include>>` | Registrar signos vitales como Observations FHIR | — siempre |
| `<<include>>` | Cerrar Encounter y actualizar Appointment | — siempre |
| `<<include>>` | Validar rangos clínicos | — siempre |
| `<<extend>>` | Sugerir código CIE-10 (LLM) | Médico ingresa diagnóstico en texto libre *(T-018)* |
| `<<extend>>` | Generar resumen del encuentro (LLM) | Encounter cerrado exitosamente *(T-017)* |
| `<<extend>>` | Crear turno retroactivo (walk-in) | Paciente sin Appointment previo |
| `<<extend>>` | Notificar consulta incompleta al médico | Médico abandona sin cerrar |
| `<<extend>>` | Mostrar advertencia de rango imposible | Signo vital fuera de rango clínico |

**Diagramas ICONIX requeridos**: dominio · casos de uso · robustez ·
secuencia · clases.

**Tareas relacionadas**: T-014 (versión simplificada del Encounter).

---

## UC-03: Calcular y notificar predicción de ausentismo

**Actor principal**: Sistema (proceso automático), con configuración
del Administrador de clínica — en el MVP configura solo las ventanas
de anticipación de recordatorios (default 48h y 24h); los umbrales de
riesgo (30%/70%) y la estrategia de canal por riesgo son fijos,
configurables post-MVP (decisión 2026-06-10).

**Flujo principal**:
1. Job programado se ejecuta cada N horas y selecciona turnos próximos
   (ventana configurable: 48h y 24h por defecto).
2. Para cada turno, el sistema obtiene el score de riesgo desde el
   motor heurístico (cache si está fresco, cálculo en vivo si expiró).
3. Según el rango del score, el sistema elige la estrategia de
   notificación:
   - Riesgo alto (>70%): notificación por Telegram con texto
     personalizado + recordatorio email 48h y 24h antes.
   - Riesgo medio (30–70%): Telegram + email 24h antes.
   - Riesgo bajo (<30%): solo email 24h antes.
4. El sistema envía las notificaciones a través del provider
   correspondiente (Telegram Bot API, SMTP (email); WhatsApp Business
   API y SMS quedan post-piloto — ADR-013).
5. El paciente puede confirmar o cancelar desde el mensaje; el sistema
   registra la respuesta y actualiza el estado del Appointment.
6. Métricas de envío y respuesta se registran para análisis posterior.

**Flujos alternativos / excepciones**:
- Fallo del provider de notificaciones → reintento con backoff +
  fallback a canal alternativo.
- Paciente sin teléfono ni email → notificar al staff de la clínica
  para llamado manual.
- Confirmación recibida → marcar Appointment como `confirmed`.
- Cancelación recibida → liberar slot y notificar al staff de la
  clínica para reasignación manual; sin lista de espera ni
  notificación automática a otros pacientes en el MVP — roadmap
  post-MVP (decisión 2026-06-10).

**Por qué no es CRUD**: orquestación temporal (jobs programados),
máquina de decisión por reglas de riesgo, integración con proveedores
externos, manejo de estados asincrónicos, reintentos y fallback,
métricas en línea.

**Recursos FHIR involucrados**: `Appointment`, `Patient`,
`Communication` (envío y respuesta).

**Relaciones UML** (5 `<<include>>` · 5 `<<extend>>`):

| Tipo | Sub-caso | Condición |
|---|---|---|
| `<<include>>` | Seleccionar turnos próximos (ventana configurable) | — siempre |
| `<<include>>` | Calcular riesgo de no-show (cache o motor) | — siempre |
| `<<include>>` | Determinar estrategia de notificación por riesgo | — siempre |
| `<<include>>` | Enviar notificación al paciente | — siempre |
| `<<include>>` | Registrar métricas de envío y respuesta | — siempre |
| `<<extend>>` | Usar score en caché (Redis) | Score fresco disponible en Redis |
| `<<extend>>` | Reintentar con backoff + fallback de canal | Falla del provider |
| `<<extend>>` | Notificar staff para llamado manual | Paciente sin teléfono ni email |
| `<<extend>>` | Marcar Appointment como `confirmed` | Respuesta del paciente = confirmación |
| `<<extend>>` | Liberar slot y notificar staff para reasignación | Respuesta del paciente = cancelación |

**Diagramas ICONIX requeridos**: dominio · casos de uso · robustez ·
secuencia · clases.

**Tareas relacionadas**: T-ML-001 (heurística), tareas nuevas de
notificaciones por crear (T-NOT-001, T-NOT-002).

---

## UC-04: Sugerir y aplicar overbooking inteligente

**Actor principal**: Secretario/a, con configuración del profesional.

**Flujo principal**:
1. Cuando UC-01 detecta un slot con riesgo alto de no-show, evalúa si
   el profesional tiene overbooking habilitado y si el tope semanal
   configurado no se ha alcanzado.
2. Si las condiciones se cumplen, el sistema sugiere reservar el mismo
   horario para un segundo turno (sobreturno).
3. El usuario decide: aceptar la sugerencia (crea segundo Appointment
   en el mismo slot, marcado como `overbooked`) o ignorarla.
4. La agenda muestra visualmente los slots con overbooking aplicado
   (icono o color diferenciado).
5. Si ambos pacientes asisten: el profesional ve la doble reserva con
   antelación y decide cómo manejarla (alargar consulta, derivar, etc.).
6. Si uno falta: el otro paciente toma el slot normalmente, el sistema
   marca al ausente con motivo "no asistió" y el contador de overbooking
   funcionó correctamente.

**Configuración por profesional**:
- Habilitar/deshabilitar overbooking.
- Umbral de riesgo para sugerir (default: 70%).
- Tope semanal de overbookings (default: 5 por semana).
- Tipos de turno excluidos (cirugías, procedimientos, primera consulta).

**Flujos alternativos / excepciones**:
- Profesional con overbooking deshabilitado → no se sugiere, turno
  normal.
- Tope semanal alcanzado → no se sugiere, se notifica al staff.
- Slot ya tiene overbooking → no se sugiere doble overbooking.
- Cancelación del primer turno → el overbooking pasa a ser turno
  normal automáticamente.

**Por qué no es CRUD**: lógica condicional compleja con múltiples
configuraciones, integración con UC-01 (no se ejecuta solo), máquina
de estados de los Appointments en el slot, balance entre eficiencia y
experiencia del paciente.

**Recursos FHIR involucrados**: `Appointment` (con extensión custom o
status `overbooked`), `Schedule`, `Slot`, `Practitioner`.

**Relaciones UML** (5 `<<include>>` · 5 `<<extend>>`):

| Tipo | Sub-caso | Condición |
|---|---|---|
| `<<include>>` | Verificar overbooking habilitado | — siempre |
| `<<include>>` | Verificar tope semanal no alcanzado | — siempre |
| `<<include>>` | Verificar slot sin overbooking previo | — siempre |
| `<<include>>` | Crear Appointment como `overbooked` | — siempre (si se acepta) |
| `<<include>>` | Mostrar indicador visual en agenda | — siempre (si se crea) |
| `<<extend>>` | Omitir sugerencia (overbooking deshabilitado) | Profesional deshabilitó overbooking |
| `<<extend>>` | Omitir y notificar staff (tope alcanzado) | Tope semanal de overbookings alcanzado |
| `<<extend>>` | Promover a turno normal | Primer turno del slot cancelado |
| `<<extend>>` | Alertar doble asistencia al profesional | Ambos pacientes asisten |
| `<<extend>>` | Excluir tipo de turno de la sugerencia | Cirugía / procedimiento / primera consulta |

**Diagramas ICONIX requeridos**: dominio · casos de uso · robustez ·
secuencia · clases.

**Tareas relacionadas**: T-011 (extensión), tareas nuevas de
overbooking por crear (T-OVB-001).

---

## Conceptos transversales (no son CU)

Estos conceptos aparecen en los 4 CU y se documentan aparte
(arquitectura, no caso de uso):

- **Multitenant**: todos los recursos FHIR llevan `tenant_id`, queries
  filtradas por middleware automáticamente.
- **FHIR R4**: validación de payloads, recursos correctamente
  estructurados, interoperabilidad nativa.
- **Obra social y tope semanal**: validación cross-cutting que aparece
  en UC-01 pero está implementada como módulo independiente.
- **Motor heurístico de no-show**: servicio interno consumido por UC-01,
  UC-03 y UC-04.

---

## Resumen de fechas de entrega

| Fecha | Entrega | UCs en scope |
|---|---|---|
| 16/06/2026 | Seminario — entrega parcial | UC-01 y UC-02 (flujos principales escritos, diagramas robustez en borrador) |
| 30/06/2026 | Seminario — consolidación | UC-01 y UC-02 (todos los diagramas) |
| 31/08/2026 | Avance interno | UC-03 (todos los diagramas) |
| 30/09/2026 | Avance interno | UC-04 (todos los diagramas), 4 CU cerrados |
| 30/11/2026 | Tesis final | 4 CU completos en tesis + implementación validada con piloto |
| Diciembre 2026 | Defensa oral | Demo + slides + tesis aprobada |

---

## Roadmap post-MVP (NO entran en el seminario)

Estos CU se mencionan en la tesis como roadmap pero no se documentan
con diagramas ICONIX ni se implementan:

- **UC-RM-01** — Alta de nuevo tenant con provisioning automático.
- **UC-RM-02** — Historial clínico SOAP avanzado con CIE-10 fuzzy y
  prescripción electrónica.
- **UC-RM-03** — Cobertura óptima por franja con forecasting ML.
- ~~UC-RM-04 — Resumen automático de historia clínica con LLM~~ y
  ~~UC-RM-05 — Sugerencia automática CIE-10~~: **promovidos al MVP el
  2026-06-08** (ADR-012, T-017/T-018) — ver CU-02 pasos 4 y 5.
- **UC-RM-06** — Marketplace B2C de turnos para pacientes finales.
