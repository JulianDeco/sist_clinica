# Especificación de casos de uso — ICONIX

**Proyecto**: Kuris — SaaS multitenant FHIR R4 para clínicas pequeñas
**Entrega**: Seminario de Trabajo Final UAI — T-020, entregable 2 (16/06/2026)
**Fuentes**: `.claude/tasks/use-cases.md` (autoritativa) · `docs/adr/ADR-013-notification-channels.md` · `docs/adr/ADR-014-multi-tenant-membership.md`
**Diagrama asociado**: `seminario/iconix/diagramas/01-casos-de-uso.mmd`

> Convención ICONIX: el curso básico se narra alternando acciones del
> actor y respuestas del sistema, en pasos numerados. Cada curso alterno
> tiene nombre y condición de disparo. Las relaciones `<<include>>` /
> `<<extend>>` listadas en cada caso de uso coinciden 1:1 con el diagrama.
> Las reglas antes marcadas **[A DEFINIR]** fueron resueltas por el
> product owner el 2026-06-10; cada una se anota *(decisión 2026-06-10)*.

---

## Catálogo de actores

| Actor | Tipo | Descripción |
|---|---|---|
| Secretario/a | Humano interno | Gestiona la agenda: reserva turnos y decide sobre sugerencias de overbooking. En consultorios unipersonales, el profesional cumple este rol. |
| Médico | Humano interno | Atiende consultas (SOAP), registra observaciones clínicas y configura su política de overbooking. |
| Administrador de clínica | Humano interno | Configura los parámetros de predicción de ausentismo y recordatorios del tenant. |
| Paciente | Humano externo | No opera el sistema: recibe notificaciones y confirma o cancela su turno desde el mensaje recibido. |
| Scheduler (job programado) | Actor temporal | Dispara la ejecución periódica de CU-03 cada N horas (convención UML de actor reloj para procesos automáticos). |

---

## CU-01 — Reservar turno con validación integral

**Actores**: Secretario/a (principal).

**Precondiciones**:
- El usuario inició sesión y seleccionó tenant (estado de sesión READY, ADR-014).
- Existen paciente y profesional registrados en el tenant.
- El profesional tiene agenda publicada (Schedule + Slot FHIR).

**Curso básico**:
1. El secretario selecciona paciente, profesional y franja horaria.
2. El sistema valida la disponibilidad del slot (Schedule + Slot FHIR) y la ausencia de conflictos *(«Validar disponibilidad de slot», `<<include>>`)*.
3. El sistema valida el tope semanal de la cobertura del paciente (Coverage): descuenta y verifica que no se supere el límite *(«Validar cobertura y tope semanal», `<<include>>`)*.
4. El sistema calcula el riesgo de ausentismo del paciente — a partir de su historial de asistencia, la anticipación de la reserva, el día y la franja — y muestra el score (0–100) junto con la lista de factores influyentes en lenguaje natural *(«Calcular riesgo de no-show», `<<include>>`)*.
5. El secretario confirma la reserva.
6. El sistema crea el `Appointment` FHIR, actualiza el Slot a `busy` y registra el score de riesgo asociado. Las tres modificaciones (Slot, Appointment, contador de cobertura) se aplican atómicamente.

**Cursos alternos**:
- **A1 — Slot ya ocupado** *(en paso 2)*: el sistema sugiere slots cercanos disponibles *(punto de extensión de «Sugerir slots alternativos»)*. Si el secretario elige uno, el flujo retoma en el paso 2 con el nuevo slot.
- **A2 — Cobertura vencida o tope superado** *(en paso 3)*: el sistema bloquea la imputación a la cobertura y muestra una advertencia a la secretaria. El secretario decide si continuar creando el turno sin imputación a cobertura — la asociación Turno—Cobertura queda vacía (multiplicidad 0..1) — o abortar la reserva *(decisión 2026-06-10)*.
- **A3 — Paciente sin historial** *(en paso 4)*: el sistema calcula el score con factores parciales y lo marca como "baja confianza". El flujo continúa en el paso 5.
- **A4 — Falla del motor heurístico** *(en paso 4)*: degradación elegante — el turno se crea sin score y queda registrado para reintento posterior. El flujo continúa en el paso 5.
- **A5 — Riesgo alto con overbooking habilitado** *(tras paso 4)*: si el score supera el 70% y el profesional tiene overbooking habilitado, se dispara CU-04 *(punto de extensión de «CU-04 Sugerir overbooking inteligente»)*.

**Postcondiciones**:
- `Appointment` creado y `Slot` en estado `busy`.
- Contador de tope semanal de la cobertura decrementado.
- Score de riesgo persistido y asociado al turno (o marcado para reintento en A4).

**Relaciones** (idénticas al diagrama):

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Validar disponibilidad de slot | — siempre |
| I2 | `<<include>>` | Validar cobertura y tope semanal | — siempre |
| I3 | `<<include>>` | Calcular riesgo de no-show | — siempre |
| I4 | `<<include>>` | Crear Appointment FHIR y actualizar Slot | — siempre |
| I5 | `<<include>>` | Registrar score de riesgo en turno | — siempre |
| E1 | `<<extend>>` | Sugerir slots alternativos | Slot solicitado ya está ocupado |
| E2 | `<<extend>>` | Advertir cobertura vencida o tope superado | Cobertura inválida o tope alcanzado |
| E3 | `<<extend>>` | Crear turno sin imputación a cobertura | Usuario confirma turno sin cobertura *(decisión 2026-06-10)* |
| E4 | `<<extend>>` | Calcular score con baja confianza | Paciente sin historial previo |
| E5 | `<<extend>>` | Registrar turno sin score para reintento | Falla del motor heurístico |
| E6 | `<<extend>>` | CU-04 Sugerir overbooking inteligente | Score > 70%, overbooking habilitado, tope no alcanzado |

**Recursos FHIR**: `Patient`, `Practitioner`, `Schedule`, `Slot`, `Appointment`, `Coverage`.

---

## CU-02 — Gestionar consulta médica (SOAP básico)

**Actores**: Médico (principal).

**Precondiciones**:
- El médico inició sesión y seleccionó tenant.
- Existe un `Appointment` en estado `arrived` (turno «llegó») para el paciente — o una consulta (`Encounter`) ya `in-progress` a retomar (curso alterno A1) — salvo curso alterno A3, walk-in.

**Curso básico**:
1. El médico abre el `Encounter` vinculado al `Appointment` existente.
2. El sistema presenta la consulta con los datos del paciente y del turno.
3. El médico registra las notas SOAP (Subjetivo, Objetivo, Análisis, Plan) en texto estructurado.
4. El sistema persiste las notas asociadas al Encounter.
5. El médico registra signos vitales y mediciones (TA, FC, peso, talla, glucemia, etc.).
6. El sistema las almacena como `Observation` FHIR asociadas al Encounter, validando rangos clínicos.
7. Opcionalmente, el médico registra un diagnóstico provisorio como descripción libre, con sugerencia automática de código CIE-10 mediante LLM (T-018, incorporado al MVP el 2026-06-08; ver extensión E1).
8. El médico cierra el Encounter.
9. El sistema cierra el Encounter (`finished`), actualiza el estado del `Appointment` a `fulfilled` y persiste todo transaccionalmente.

**Cursos alternos**:
- **A1 — Abandono sin cerrar** *(en cualquier paso antes del 8)*: el Encounter queda en estado `in-progress` y el sistema notifica al médico en su próxima sesión.
- **A2 — Valores clínicos fuera de rango** *(en paso 6)*: ante rangos imposibles, el sistema emite una advertencia pero no bloquea el registro.
- **A3 — Paciente sin Appointment previo (walk-in)** *(en paso 1)*: el sistema permite crear un Encounter "walk-in" que genera el Appointment retroactivo *(punto de extensión de «Crear turno retroactivo (walk-in)»)*. El flujo continúa en el paso 2.

**Postcondiciones**:
- Encounter en estado `finished` con notas SOAP y Observations asociadas.
- `Appointment` en estado `fulfilled`.
- Máquinas de estado respetadas — son dos máquinas separadas *(decisión 2026-06-10)*:
  - Turno (FHIR `Appointment`): `reservado → confirmado → llegó → cumplido / ausente`.
  - ConsultaMedica (FHIR `Encounter`): `en curso → finalizada`.

**Relaciones** (idénticas al diagrama):

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Abrir / reanudar Encounter vinculado | — siempre |
| I2 | `<<include>>` | Registrar notas SOAP estructuradas | — siempre |
| I3 | `<<include>>` | Registrar signos vitales como Observations FHIR | — siempre |
| I4 | `<<include>>` | Cerrar Encounter y actualizar Appointment | — siempre |
| I5 | `<<include>>` | Validar rangos clínicos | — siempre |
| E1 | `<<extend>>` | Sugerir código CIE-10 (LLM) | Médico ingresa diagnóstico en texto libre *(T-018)* |
| E2 | `<<extend>>` | Generar resumen del encuentro (LLM) | Encounter cerrado exitosamente *(T-017)* |
| E3 | `<<extend>>` | Crear turno retroactivo (walk-in) | Paciente sin Appointment previo |
| E4 | `<<extend>>` | Notificar consulta incompleta al médico | Médico abandona la consulta sin cerrarla |
| E5 | `<<extend>>` | Mostrar advertencia de rango imposible | Signo vital fuera de rango clínico |

**Recursos FHIR**: `Encounter`, `Observation`, `Appointment`, `Patient`, `Practitioner`.

---

## CU-03 — Calcular y notificar predicción de ausentismo

**Actores**: Scheduler / job programado (principal) · Paciente (secundario, receptor) · Administrador de clínica (configura previamente, ver «Configurar predicción y recordatorios»).

**Precondiciones**:
- Ventanas de notificación configuradas (default: 48 h y 24 h antes del turno).
- Existen turnos próximos dentro de la ventana.
- Providers de notificación configurados (Telegram Bot API y SMTP — canales MVP según ADR-013; WhatsApp y SMS quedan en roadmap post-piloto).

**Curso básico**:
1. El Scheduler dispara el job programado cada N horas.
2. El sistema selecciona los turnos próximos dentro de la ventana configurable.
3. Para cada turno, el sistema obtiene o recalcula el score de riesgo de no-show *(«Calcular riesgo de no-show», `<<include>>`)*.
4. El sistema elige la estrategia de notificación según el rango del score — el canal por riesgo se cursa vía Telegram/email (canales MVP según ADR-013):
   - Riesgo alto (>70%): Telegram con texto personalizado + recordatorio email 48 h y 24 h antes.
   - Riesgo medio (30–70%): Telegram + email 24 h antes.
   - Riesgo bajo (<30%): solo email 24 h antes.
5. El sistema envía las notificaciones a través del provider correspondiente *(«Enviar notificación», `<<include>>`)*.
6. El paciente confirma o cancela desde el mensaje; el sistema registra la respuesta y actualiza el estado del `Appointment` *(ver caso de uso de soporte «Registrar respuesta del paciente»)*.
7. El sistema registra métricas de envío y respuesta para análisis posterior.

**Cursos alternos**:
- **A1 — Fallo del provider de notificaciones** *(en paso 5)*: el sistema reintenta con backoff y, si persiste, hace fallback a un canal alternativo.
- **A2 — Paciente sin teléfono ni email** *(en paso 5)*: el sistema notifica al staff de la clínica para realizar el llamado manual *(punto de extensión de «Notificar staff para llamado manual»)*.
- **A3 — Confirmación recibida** *(en paso 6)*: el sistema marca el `Appointment` como `confirmed`.
- **A4 — Cancelación recibida** *(en paso 6)*: el sistema libera el slot y notifica al staff de la clínica para la reasignación manual *(extensión «Reasignar slot liberado» sobre «Registrar respuesta del paciente»)*. No hay lista de espera ni notificación automática a otros pacientes en el MVP — queda en roadmap post-MVP *(decisión 2026-06-10)*.

**Postcondiciones**:
- Notificaciones enviadas según estrategia de riesgo (o derivadas al staff en A2).
- Respuestas del paciente registradas y estado del `Appointment` actualizado.
- Métricas de envío y respuesta persistidas.

**Relaciones** (idénticas al diagrama):

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Seleccionar turnos próximos (ventana) | — siempre |
| I2 | `<<include>>` | Calcular riesgo de no-show | — siempre *(reutiliza el mismo motor que CU-01)* |
| I3 | `<<include>>` | Determinar estrategia de notificación por riesgo | — siempre |
| I4 | `<<include>>` | Enviar notificación al paciente | — siempre |
| I5 | `<<include>>` | Registrar métricas de envío y respuesta | — siempre |
| E1 | `<<extend>>` | Usar score calculado previamente | Score del turno todavía vigente (no requiere recálculo) |
| E2 | `<<extend>>` | Reintentar notificación con backoff + fallback | Falla del provider de notificaciones |
| E3 | `<<extend>>` | Notificar staff para llamado manual | Paciente sin teléfono ni email |
| E4 | `<<extend>>` | Marcar Appointment como `confirmed` | Respuesta del paciente es confirmación |
| E5 | `<<extend>>` | Liberar slot y notificar staff para reasignación | Respuesta del paciente es cancelación |

**Recursos FHIR**: `Appointment`, `Patient`, `Communication` (envío y respuesta).

---

## CU-04 — Sugerir y aplicar overbooking inteligente

**Actores**: Secretario/a (principal) · Médico (configura previamente su política, ver «Configurar overbooking»).

**Precondiciones**:
- CU-01 en ejecución con score de riesgo ya calculado (este caso de uso no se ejecuta de forma autónoma: extiende a CU-01).
- El profesional tiene overbooking habilitado.

**Configuración por profesional** (gestionada en «Configurar overbooking»):
- Habilitar/deshabilitar overbooking.
- Umbral de riesgo para sugerir (default: 70%).
- Tope semanal de overbookings (default: 5 por semana).
- Tipos de turno excluidos (cirugías, procedimientos, primera consulta).

**Curso básico**:
1. El sistema, durante CU-01, detecta un slot con riesgo alto de no-show (score > umbral configurado).
2. El sistema verifica que el profesional tenga overbooking habilitado y que el tope semanal configurado no se haya alcanzado.
3. El sistema sugiere reservar el mismo horario para un segundo turno (sobreturno).
4. El secretario acepta la sugerencia.
5. El sistema crea el segundo `Appointment` en el mismo slot, marcado como `overbooked`.
6. El sistema muestra visualmente en la agenda los slots con overbooking aplicado (ícono o color diferenciado).

**Cursos alternos**:
- **A1 — El secretario ignora la sugerencia** *(en paso 4)*: el turno original se crea normalmente, sin sobreturno.
- **A2 — Overbooking deshabilitado** *(en paso 2)*: no se sugiere; el turno continúa como turno normal en CU-01.
- **A3 — Tope semanal alcanzado** *(en paso 2)*: no se sugiere y el sistema notifica al staff.
- **A4 — Slot ya tiene overbooking** *(en paso 2)*: no se sugiere doble overbooking.
- **A5 — Tipo de turno excluido** *(en paso 2)*: si el turno es de un tipo excluido por configuración (cirugía, procedimiento, primera consulta), no se sugiere.
- **A6 — Cancelación del primer turno** *(posterior)*: el overbooking pasa a ser turno normal automáticamente.
- **A7 — Ambos pacientes asisten** *(operativo, posterior)*: el profesional ve la doble reserva con antelación y decide cómo manejarla (alargar consulta, derivar, etc.).
- **A8 — Uno de los pacientes falta** *(operativo, posterior)*: el otro paciente toma el slot normalmente y el sistema marca al ausente con motivo "no asistió".

**Postcondiciones**:
- Si se aceptó: segundo `Appointment` con marca `overbooked` en el mismo slot, visible diferenciado en la agenda, y contador semanal de overbookings incrementado.
- Si no se aceptó o no aplicó: estado del sistema sin cambios respecto de CU-01.

**Relaciones** (idénticas al diagrama):

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Verificar overbooking habilitado | — siempre |
| I2 | `<<include>>` | Verificar tope semanal no alcanzado | — siempre |
| I3 | `<<include>>` | Verificar slot sin overbooking previo | — siempre |
| I4 | `<<include>>` | Crear Appointment como `overbooked` | — siempre (si se acepta la sugerencia) |
| I5 | `<<include>>` | Mostrar indicador visual en agenda | — siempre (si se crea el sobreturno) |
| E1 | `<<extend>>` | Omitir sugerencia (overbooking deshabilitado) | Profesional deshabilitó overbooking |
| E2 | `<<extend>>` | Omitir y notificar staff (tope semanal alcanzado) | Tope semanal de overbookings alcanzado |
| E3 | `<<extend>>` | Promover a turno normal (cancelación) | Primer turno del slot es cancelado |
| E4 | `<<extend>>` | Alertar doble asistencia al profesional | Ambos pacientes asisten al mismo slot |
| E5 | `<<extend>>` | Excluir tipo de turno de la sugerencia | Turno es cirugía, procedimiento o primera consulta |

CU-04 es extendido por CU-01: condición de disparo → score > 70% (umbral configurable), overbooking habilitado y tope semanal no alcanzado.

**Recursos FHIR**: `Appointment` (con extensión custom o status `overbooked`), `Schedule`, `Slot`, `Practitioner`.

---

# Casos de uso de soporte

Especificación breve de los casos de uso introducidos en el diagrama
que no forman parte de los 4 CU core.

## CU-S01 — Iniciar sesión

**Actores**: Secretario/a, Médico, Administrador de clínica.
**Descripción**: autenticación two-step según ADR-014: identidad separada de tenancy.
**Curso básico**: (1) el usuario ingresa email y contraseña; (2) el sistema valida las credenciales y emite un *identity token* de corta duración (5 min) junto con la lista de tenants a los que pertenece; (3) se ejecuta siempre la selección de tenant (`<<include>>` → Seleccionar tenant).
**Curso alterno**: A1 — credenciales inválidas: el sistema rechaza el acceso e informa el error.
**Relaciones**: `<<include>>` → Seleccionar tenant.

## CU-S02 — Seleccionar tenant

**Actores**: (incluido desde Iniciar sesión).
**Descripción**: segundo paso del flujo de auth (ADR-014). Convierte el identity token en una sesión de trabajo sobre un tenant concreto.
**Curso básico**: (1) el sistema presenta la lista de tenants del usuario con su rol en cada uno; (2) el usuario elige uno; (3) el sistema emite el *access token* de sesión (30 min, con `tenant_id` y `role`) y el refresh token (cookie httpOnly, 7 días).
**Curso alterno**: A1 — usuario con un único tenant: el sistema lo selecciona automáticamente, sin interacción (por eso la relación es `<<include>>`: siempre se ejecuta, con o sin pantalla).
**Relaciones**: incluido por Iniciar sesión.

## CU-S03 — Validar disponibilidad de slot

**Descripción**: verifica que el slot solicitado esté libre en el Schedule del profesional y que no existan conflictos. Sub-funcionalidad obligatoria de CU-01.
**Relaciones**: incluido por CU-01.

## CU-S04 — Validar cobertura y tope semanal

**Descripción**: verifica la vigencia de la cobertura (`Coverage` FHIR) del paciente y que el tope semanal de la obra social no se supere; descuenta del contador. Módulo cross-cutting independiente (ver «Conceptos transversales» en use-cases.md).
**Relaciones**: incluido por CU-01.

## CU-S05 — Calcular riesgo de no-show

**Descripción**: motor heurístico explicable. Recibe historial del paciente, anticipación de la reserva, día y franja horaria; devuelve score 0–100 más la lista de factores influyentes en lenguaje natural. Servicio interno consumido por CU-01, CU-03 y (a través de CU-01) CU-04 — por eso se modela una sola vez y se reutiliza vía `<<include>>`.
**Relaciones**: incluido por CU-01 y por CU-03.

## CU-S06 — Enviar notificación

**Descripción**: envía la notificación por el provider correspondiente al canal elegido (Telegram Bot API o SMTP — canales MVP según ADR-013; WhatsApp y SMS en roadmap post-piloto) según la estrategia de riesgo. El Paciente es el actor receptor. Incluye reintento con backoff y fallback a canal alternativo ante fallo del provider.
**Relaciones**: incluido por CU-03.

## CU-S07 — Registrar respuesta del paciente

**Actores**: Paciente.
**Descripción**: el paciente confirma o cancela el turno desde el mensaje recibido; el sistema registra la respuesta, actualiza el `Appointment` (`confirmed` ante confirmación) y registra métricas.
**Curso alterno**: A1 — cancelación: se libera el slot y se dispara la extensión «Reasignar slot liberado».
**Relaciones**: es extendido por Reasignar slot liberado.

## CU-S08 — Sugerir slots alternativos

**Descripción**: ante un slot ya ocupado durante CU-01, el sistema propone slots cercanos disponibles para que el secretario elija.
**Relaciones**: `<<extend>>` → CU-01. Condición: el slot solicitado ya está ocupado.

## CU-S09 — Crear turno retroactivo (walk-in)

**Descripción**: ante una atención sin turno previo, permite crear un Encounter "walk-in" que genera el `Appointment` retroactivo, manteniendo la integridad referencial Encounter–Appointment.
**Relaciones**: `<<extend>>` → CU-02. Condición: paciente sin Appointment previo.

## CU-S10 — Notificar staff para llamado manual

**Descripción**: cuando el paciente no tiene teléfono ni email registrados, el sistema notifica al staff de la clínica para que realice el contacto telefónico manual.
**Relaciones**: `<<extend>>` → CU-03. Condición: paciente incontactable por canales digitales.

## CU-S11 — Reasignar slot liberado

**Descripción**: ante la cancelación de un turno, libera la franja horaria y notifica al staff de la clínica (mismo mecanismo de aviso al staff de CU-S10) para la reasignación manual del slot. No hay entidad ListaDeEspera ni notificación automática a otros pacientes en el MVP — queda en roadmap post-MVP *(decisión 2026-06-10)*.
**Relaciones**: `<<extend>>` → Registrar respuesta del paciente. Condición: la respuesta es una cancelación.

## CU-S12 — Configurar overbooking

**Actores**: Médico.
**Descripción**: el profesional gestiona su política de overbooking: habilitación, umbral de riesgo (default 70%), tope semanal (default 5) y tipos de turno excluidos.
**Relaciones**: sin include/extend (configuración consultada por CU-04).

## CU-S13 — Configurar predicción y recordatorios

**Actores**: Administrador de clínica.
**Descripción**: el administrador configura los parámetros del proceso automático de CU-03. En el MVP es configurable SOLO la ventana de anticipación de los recordatorios (default 48 h y 24 h). Los umbrales de riesgo (30%/70%) y la estrategia de canal por riesgo son fijos en el MVP — configurables post-MVP *(decisión 2026-06-10)*.
**Relaciones**: sin include/extend (configuración consultada por CU-03).

## CU-S14 — Cambiar de clínica activa

**Actores**: Secretario/a, Médico, Administrador de clínica.
**Precondición**: sesión activa sobre una clínica (estado READY, ADR-014).
**Descripción**: tercer flujo de autenticación de ADR-014 (*switch-tenant*): el usuario con membresía en varias clínicas cambia de clínica activa sin volver a ingresar credenciales. Ya implementado en el navbar del frontend (T-002).
**Curso básico**: (1) el usuario solicita el cambio de clínica activa; (2) el sistema valida su pertenencia a la clínica destino (membresía activa); (3) el sistema emite una nueva sesión para esa clínica y revoca la anterior; (4) el sistema recarga el contexto de trabajo sobre la clínica elegida.
**Curso alterno**: A1 — el usuario no pertenece a la clínica destino: el sistema rechaza el cambio y conserva la sesión vigente.
**Relaciones**: sin include/extend (los totales del diagrama no cambian).

---

## Matriz de trazabilidad de relaciones (diagrama ↔ texto)

### CU-01 — Reservar turno con validación integral

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Validar disponibilidad de slot | — |
| I2 | `<<include>>` | Validar cobertura y tope semanal | — |
| I3 | `<<include>>` | Calcular riesgo de no-show | — |
| I4 | `<<include>>` | Crear Appointment FHIR y actualizar Slot | — |
| I5 | `<<include>>` | Registrar score de riesgo en turno | — |
| E1 | `<<extend>>` | Sugerir slots alternativos | Slot ocupado |
| E2 | `<<extend>>` | Advertir cobertura vencida o tope superado | Cobertura inválida |
| E3 | `<<extend>>` | Crear turno sin imputación a cobertura | Usuario confirma sin cobertura |
| E4 | `<<extend>>` | Calcular score con baja confianza | Paciente sin historial |
| E5 | `<<extend>>` | Registrar turno sin score para reintento | Falla del motor heurístico |
| E6 | `<<extend>>` | CU-04 Sugerir overbooking inteligente | Score > 70% + overbooking habilitado + tope no alcanzado |

### CU-02 — Gestionar consulta médica (SOAP básico)

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Abrir / reanudar Encounter vinculado | — |
| I2 | `<<include>>` | Registrar notas SOAP estructuradas | — |
| I3 | `<<include>>` | Registrar signos vitales como Observations FHIR | — |
| I4 | `<<include>>` | Cerrar Encounter y actualizar Appointment | — |
| I5 | `<<include>>` | Validar rangos clínicos | — |
| E1 | `<<extend>>` | Sugerir código CIE-10 (LLM) | Diagnóstico en texto libre |
| E2 | `<<extend>>` | Generar resumen del encuentro (LLM) | Encounter cerrado |
| E3 | `<<extend>>` | Crear turno retroactivo (walk-in) | Paciente sin Appointment previo |
| E4 | `<<extend>>` | Notificar consulta incompleta al médico | Médico abandona sin cerrar |
| E5 | `<<extend>>` | Mostrar advertencia de rango imposible | Signo vital fuera de rango |

### CU-03 — Calcular y notificar predicción de ausentismo

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Seleccionar turnos próximos (ventana) | — |
| I2 | `<<include>>` | Calcular riesgo de no-show | — |
| I3 | `<<include>>` | Determinar estrategia de notificación por riesgo | — |
| I4 | `<<include>>` | Enviar notificación al paciente | — |
| I5 | `<<include>>` | Registrar métricas de envío y respuesta | — |
| E1 | `<<extend>>` | Usar score calculado previamente | Score del turno todavía vigente (no requiere recálculo) |
| E2 | `<<extend>>` | Reintentar con backoff + fallback | Falla del provider |
| E3 | `<<extend>>` | Notificar staff para llamado manual | Paciente incontactable |
| E4 | `<<extend>>` | Marcar Appointment como `confirmed` | Respuesta = confirmación |
| E5 | `<<extend>>` | Liberar slot y notificar staff | Respuesta = cancelación |

### CU-04 — Sugerir y aplicar overbooking inteligente

| # | Tipo | Sub-caso / Extensión | Condición |
|---|---|---|---|
| I1 | `<<include>>` | Verificar overbooking habilitado | — |
| I2 | `<<include>>` | Verificar tope semanal no alcanzado | — |
| I3 | `<<include>>` | Verificar slot sin overbooking previo | — |
| I4 | `<<include>>` | Crear Appointment como `overbooked` | — |
| I5 | `<<include>>` | Mostrar indicador visual en agenda | — |
| E1 | `<<extend>>` | Omitir sugerencia (overbooking deshabilitado) | Overbooking off |
| E2 | `<<extend>>` | Omitir y notificar staff (tope alcanzado) | Tope semanal alcanzado |
| E3 | `<<extend>>` | Promover a turno normal | Primer turno cancelado |
| E4 | `<<extend>>` | Alertar doble asistencia | Ambos pacientes asisten |
| E5 | `<<extend>>` | Excluir tipo de turno | Cirugía / procedimiento / primera consulta |

**Totales por caso de uso**: 5 `<<include>>` · 5–6 `<<extend>>` por CU (requisito mínimo de la cátedra: 5 de cada tipo por CU).
Los casos de uso de soporte (CU-S12, CU-S13, CU-S14) y la relación auth (`Iniciar sesión` → `Seleccionar tenant`) no cuentan en los totales de los CU core.
