# Glosario del Dominio — ClinicaSaaS (ICONIX)

**Entrega**: T-020 — deliverable 3 · Seminario de Trabajo Final UAI 2026 · 16/06/2026
**Diagrama asociado**: `seminario/iconix/diagramas/02-dominio.puml` (correspondencia 1:1)

> En ICONIX, el modelo de dominio **es** el glosario del proyecto: cada
> entidad del diagrama tiene aquí su definición en lenguaje del problema
> (el mundo real de la clínica), sin conceptos técnicos. La columna
> *Fuente* garantiza la trazabilidad: ninguna entidad fue inventada —
> todas provienen de los casos de uso (`.claude/tasks/use-cases.md`),
> del esquema de base de datos (`DB-Schema.spec.md`) o de un ADR.

---

## Términos del dominio

| Término | Definición | Fuente |
|---|---|---|
| **Clinica** | Establecimiento de salud (1–5 profesionales) que contrata el servicio. Es la frontera de aislamiento de todos los datos: pacientes, agendas y turnos pertenecen siempre a una clínica. | Transversal CU-01..04 · tabla `tenants` |
| **Usuario** | Persona del personal interno (administrador, médico, secretaria) con identidad única en el sistema, independiente de la clínica donde trabaje. | CU-01, CU-02 · tabla `users` · ADR-014 |
| **Membresia** | Vínculo entre un Usuario y una Clinica: indica que esa persona trabaja en esa clínica y con qué Rol. Una persona puede tener varias membresías (p. ej., un médico con dos consultorios). | ADR-014 · tabla `user_tenants` |
| **Rol** | Función que desempeña un usuario dentro de una clínica: Administrador, Médico o Secretario/a. Determina qué acciones puede realizar. | CU-01..04 (actores) · tabla `roles` |
| **Paciente** | Persona que recibe atención médica en la clínica. Es el sujeto de turnos, consultas, coberturas y recordatorios. | CU-01, CU-02, CU-03 · `fhir_resources` (Patient) |
| **Profesional** | Médico u otro profesional de la salud que atiende pacientes en la clínica y posee una agenda de atención. | CU-01, CU-02, CU-04 · `fhir_resources` (Practitioner) |
| **Agenda** | Plan de atención de un profesional: el conjunto de franjas horarias que ofrece para recibir pacientes. | CU-01 · `fhir_resources` (Schedule) |
| **FranjaHoraria** | Intervalo de tiempo concreto dentro de una agenda (p. ej., martes 10:00–10:20) que puede estar libre u ocupado por un turno. Parte constitutiva de la Agenda. | CU-01, CU-04 · `fhir_resources` (Slot) |
| **Turno** | Reserva de una franja horaria para que un profesional atienda a un paciente en fecha y hora determinadas. Pasa por estados (reservado, confirmado, cumplido, cancelado, ausente). | CU-01..04 · tabla `appointments` |
| **Sobreturno** | Tipo especial de Turno que comparte la franja horaria con un turno ya reservado de alto riesgo de ausentismo. Es la materialización del overbooking inteligente. | CU-04 · `appointments.is_overbooked` |
| **PoliticaDeSobreturnos** | Reglas que cada profesional define para los sobreturnos: si los acepta, umbral de riesgo para sugerirlos, tope semanal y tipos de turno excluidos. | CU-04 (configuración) · tabla `overbooking_config` |
| **ConsultaMedica** | Encuentro de atención entre un profesional y un paciente, generalmente originado por un turno. Tiene un ciclo de vida (planificada, en curso, finalizada). Un turno puede dar lugar a más de una consulta (reingreso, atención dividida). | CU-02 · tabla `encounters` (OQ-10) |
| **NotaSOAP** | Registro clínico estructurado de una consulta en cuatro secciones: Subjetivo, Objetivo, Análisis y Plan. No existe fuera de su consulta. | CU-02 · `fhir_resources` (Encounter) |
| **Observacion** | Medición o signo vital registrado durante una consulta: tensión arterial, frecuencia cardíaca, peso, talla, glucemia, etc. | CU-02 · `fhir_resources` (Observation) |
| **ObraSocial** | Entidad financiadora de salud (obra social o prepaga) que cubre, total o parcialmente, las prestaciones de sus afiliados. | CU-01 · `fhir_resources` (Coverage — payor) |
| **Cobertura** | Afiliación de un paciente a una obra social, con un tope semanal de prestaciones. Cada turno cubierto se imputa contra ese tope. | CU-01 · `fhir_resources` (Coverage) · tabla `coverage_weekly_usage` |
| **PrediccionDeRiesgo** | Estimación explicable (0–100) de la probabilidad de que un paciente no asista a su turno, junto con los factores que la justifican en lenguaje natural. Un turno puede recalcularse varias veces. | CU-01, CU-03, CU-04 · tabla `appointment_noshow_scores` |
| **Recordatorio** | Mensaje enviado al paciente sobre su turno próximo, por el canal y con la anticipación que corresponden a su nivel de riesgo. El paciente puede confirmar o cancelar desde el mensaje. | CU-03 · tabla `notification_log` · `fhir_resources` (Communication) |

---

## Relaciones destacadas (decisiones de modelado)

| Decisión | Justificación |
|---|---|
| **Sobreturno ⊳ Turno (generalización)** | En el mundo real un sobreturno *es* un turno — mismo paciente, profesional y franja — con la regla especial de compartir franja. En la BD se refleja como bandera (`is_overbooked`), pero en el espacio del problema la relación natural es "es-un" (CU-04). |
| **Agenda ◇— FranjaHoraria (agregación)** | Todo–parte verdadero: la franja horaria no tiene sentido fuera de la agenda que la contiene. Única agregación del modelo junto con NotaSOAP. |
| **ConsultaMedica ◆— NotaSOAP (composición)** | La nota SOAP es parte inseparable de su consulta; no existe de forma independiente. |
| **Usuario ↔ Membresia ↔ Clinica** | ADR-014: la identidad (Usuario) se separa de la pertenencia (Membresia). El Rol se asocia a la membresía, no al usuario: la misma persona puede ser Médico en una clínica y Administrador en otra. |
| **Usuario 0..1 — 0..1 Profesional** | El médico que usa el sistema es a la vez Usuario (inicia sesión) y Profesional (tiene agenda). No todo usuario es profesional (secretaria) ni todo profesional es usuario. Ver *Hallazgos* abajo. |
| **Turno —0..1 Cobertura ("se imputa a")** | CU-01 paso 3: al reservar, el turno descuenta del tope semanal de la cobertura. El turno particular (sin cobertura) es válido — multiplicidad 0..1. |
| **FranjaHoraria 1 — 0..\* Turno** | Normalmente una franja aloja un solo turno; la multiplicidad 0..\* habilita el segundo turno del sobreturno (CU-04). |

---

## Términos evaluados y excluidos

| Término | Motivo de exclusión |
|---|---|
| **ListaDeEspera** | Aparece solo de forma eventual en un flujo alternativo de CU-03 ("eventualmente notificar otro paciente en lista de espera"). No tiene tabla en BD ni flujo principal que la requiera en el MVP. Se incorporará al modelo si entra en scope. |
| **Notificacion (genérica) / Canal** | El canal concreto (Telegram, email, SMS) es una decisión de implementación (ADR-013), no un concepto del problema. El concepto del dominio es **Recordatorio**. |
| **Tenant, JWT, Token, Cache, Score** | Conceptos técnicos del espacio de la solución. En el dominio: Tenant → Clinica; Score → PrediccionDeRiesgo. |
