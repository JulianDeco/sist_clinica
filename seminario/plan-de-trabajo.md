Kuris — Plan de Trabajo
Seminario de Trabajo Final — UAI 2026
Autor: Julián Deco
Versión: 1.1 — 12/05/2026
================================================================


OBJETIVO

Entregar el MVP del seminario antes del 30/11/2026, con un mes de
holgura (diciembre) para correcciones, validación con piloto y defensa.

CARGA REAL ESTIMADA

  - Disponibilidad: 15-20 h/semana en promedio.
  - Trabajo en programación / IT: aporta transferencia de aprendizaje
    pero fatiga mental similar al final del día.
  - Deporte: menos de 6 h/semana.
  - Ventana total: 32 semanas (12/05/2026 a 31/12/2026) =
    aproximadamente 480-640 horas de trabajo efectivo.

ALCANCE DEL MVP (RECORTADO PARA CARGA REAL)

Funcionalidades dentro del MVP:

  - FHIR R4 multitenant: Patient, Practitioner, Appointment, Encounter,
    Observation, Coverage.
  - Agenda funcional, ficha de paciente, historia clínica básica
    (SOAP), reporte mensual de ausentismo.
  - Obra social con validación de tope semanal.
  - Predicción heurística explicable de ausentismo.
  - Recordatorios inteligentes diferenciados por riesgo.
  - Overbooking inteligente.
  - 1 piloto real (o plan B con simulación documentada).

Funcionalidades movidas a roadmap post-MVP:

  - Resumen automático de historia clínica con LLM.
  - Sugerencia automática de clasificación CIE-10.
  - Sugerencia de cobertura óptima por franja horaria.
  - Modelos de Machine Learning entrenados por tenant.
  - Importador desde Excel para migración masiva (versión simplificada
    de carga manual en MVP).

CASOS DE USO CORE PARA ICONIX (4)

La documentación ICONIX se concentra en cuatro casos de uso complejos,
no en CRUDs simples. Multitenant, FHIR y obra social aparecen como
conceptos transversales en todos.

  CU-01 — Reservar turno con validación integral.
          Validación de slot, tope semanal de obra social, cálculo de
          riesgo de ausentismo y decisión de overbooking. Flujos
          alternativos múltiples.

  CU-02 — Gestionar consulta médica (SOAP).
          Apertura de Encounter, registro de Observations y Condition,
          estados del encuentro, validaciones clínicas, cierre.

  CU-03 — Calcular y notificar predicción de ausentismo.
          Trigger automático sobre turnos próximos, cálculo de score,
          selección de canal según riesgo, envío, tracking.

  CU-04 — Sugerir y aplicar overbooking.
          Detección de slots con alto riesgo, verificación de tope
          configurado, propuesta, confirmación, ajuste de capacidad.

Cada caso de uso entrega: diagrama de robustez (boundary / control /
entity), diagrama de secuencia, descripción narrativa con flujo
principal + alternativos + excepciones, y modelo de dominio actualizado.

PRINCIPIOS DEL PLAN

  1. Flujo por función: implementar → test → commit. Nunca acumular.
  2. Backend antes que frontend para cada feature.
  3. La tesis se escribe en paralelo. Cada feature termina con 1-2
     páginas de documentación.
  4. La IA asiste, no decide.
  5. Cada hito tiene condición de "terminado" verificable.
  6. Si una semana se cae (trabajo intenso, lesión, vacación), no se
     recupera al toque: el cronograma absorbe con buffers planeados.


================================================================
FASE 1 — CIMIENTOS Y AGENDA (12/05/2026 a 12/07/2026, 9 semanas)
================================================================


SEMANA 1 (12/05 – 18/05) — Multitenant en base de datos

Objetivos:
  - Aislamiento por tenant_id en modelos existentes.
  - Middleware FastAPI que extrae tenant del JWT.
  - Tests verifican que tenant A no lee datos de tenant B.

Entregable:
  - Migración Alembic con tenant_id.
  - Tests de aislamiento pasando.
  - Documento backend/docs/multitenancy.md.

Carga estimada: 12-15 h.


SEMANA 2 (19/05 – 25/05) — Patient + Practitioner FHIR

Objetivos:
  - Modelos SQLAlchemy + tablas FHIR.
  - Endpoints /fhir/R4/Patient y /fhir/R4/Practitioner.
  - Validación con fhir.resources.

Entregable:
  - CRUDs FHIR funcionales con tests.

Carga estimada: 15-18 h.


SEMANA 3 (26/05 – 01/06) — Appointment backend

Objetivos:
  - Recurso FHIR Appointment con estados.
  - Endpoints de listado y conflictos de horario.
  - Endpoint /agenda/calendar optimizado.

Entregable:
  - Appointment funcional con tests de happy path y conflictos.

Carga estimada: 15-18 h.


SEMANA 4 (02/06 – 08/06) — Frontend base + login

Objetivos:
  - Next.js App Router con OAuth2.
  - Layout principal y manejo de sesión.
  - Página de dashboard accesible (vacía).

Entregable:
  - Flujo login → dashboard contra backend real.

Carga estimada: 15-18 h.


SEMANA 5 (09/06 – 15/06) — Frontend de agenda

Objetivos:
  - Calendario interactivo.
  - Modal de creación / edición de turno.
  - Vista semanal y diaria.

Entregable:
  - Página /agenda funcional contra backend.
  - Avance ICONIX: borrador de CU-01 (flujo principal).

Hito entrega parcial seminario: 16/06/2026.

Carga estimada: 18-20 h.


SEMANA 6 (16/06 – 22/06) — Pacientes (CRUD frontend + backend
completado)

Objetivos:
  - Listado con búsqueda y paginación.
  - Ficha demográfica.

Entregable:
  - /pacientes y /pacientes/{id} funcionales.

Carga estimada: 12-15 h.


SEMANA 7 (23/06 – 30/06) — Profesionales + consolidación

Objetivos:
  - CRUD frontend de profesionales.
  - Asignación de turnos por profesional.
  - Refactor de deuda técnica acumulada.

Entregable:
  - Módulos Patient / Practitioner / Appointment end-to-end.

Hito entrega parcial seminario: 30/06/2026.

Carga estimada: 12-15 h.


SEMANA 8 (01/07 – 06/07) — BUFFER 1

Semana de absorción de retrasos acumulados. Si todo va en hora,
adelantar inicio de Fase 2 (Encounter backend).

Carga planeada: 8-12 h (semana de descompresión).


SEMANA 9 (07/07 – 13/07) — Encounter + Observation backend

Objetivos:
  - Recursos FHIR Encounter y Observation.
  - Relación Encounter → Observations.
  - Endpoints funcionales.

Entregable:
  - Backend de consulta médica listo.
  - Avance ICONIX: diagrama de robustez de CU-02.

Carga estimada: 15-18 h.


================================================================
FASE 2 — HISTORIA CLÍNICA Y OBRA SOCIAL (13/07/2026 a 16/08/2026,
5 semanas)
================================================================


SEMANA 10 (14/07 – 20/07) — Historia clínica frontend

Objetivos:
  - Timeline de consultas.
  - Formulario SOAP simple.
  - Visualización de mediciones.

Entregable:
  - Página /pacientes/{id}/historia funcional.
  - CU-02 (gestionar consulta SOAP) — flujo principal completo.

Carga estimada: 18-20 h.


SEMANA 11 (21/07 – 27/07) — Obra social: modelo y carga

Objetivos:
  - Recurso FHIR Coverage.
  - Tabla estática de obras sociales.
  - Asignación de cobertura a paciente.

Entregable:
  - CRUD Coverage funcionando.

Carga estimada: 12-15 h.


SEMANA 12 (28/07 – 03/08) — Obra social: validación de tope semanal

Objetivos:
  - Validador de tope semanal en creación de turno.
  - Integración con flujo de reserva.

Entregable:
  - CU-01 (reservar turno) avanza: incluye validación de cobertura.

Carga estimada: 15-18 h.


SEMANA 13 (04/08 – 10/08) — Reporte mensual de ausentismo

Objetivos:
  - Endpoint de reporte.
  - Página /reportes/ausentismo con tabla y gráfico simple.

Entregable:
  - Reporte funcionando.

Carga estimada: 12-15 h.


SEMANA 14 (11/08 – 16/08) — BUFFER 2 + cierre ICONIX parcial

Objetivos:
  - Absorber retrasos.
  - Cerrar documentación de CU-01 y CU-02 (texto narrativo completo,
    diagramas finales).

Entregable:
  - CU-01 y CU-02 con documentación ICONIX completa.

Carga estimada: 10-15 h.


================================================================
FASE 3 — INTELIGENCIA OPERACIONAL (17/08/2026 a 27/09/2026, 6 semanas)
================================================================


SEMANA 15 (17/08 – 23/08) — Motor de predicción heurística (backend)

Objetivos:
  - Servicio que calcula probabilidad de no-show.
  - Factores explicables.
  - Endpoint /turnos/{id}/riesgo-noshow.

Entregable:
  - Predicción funcionando con tests.
  - Documento backend/docs/noshow.md.

Carga estimada: 18-20 h.


SEMANA 16 (24/08 – 30/08) — Indicador visual + integración con agenda

Objetivos:
  - Semáforo de riesgo en la agenda.
  - Panel explicativo de factores por turno.

Entregable:
  - Riesgo visible en UI.
  - CU-03 (calcular y notificar predicción) — diagrama de robustez.

Carga estimada: 15-18 h.


SEMANA 17 (31/08 – 06/09) — Recordatorios inteligentes (parte 1)

Objetivos:
  - Integración con WhatsApp Business API o fallback email/SMS.
  - Plantillas configurables.

Entregable:
  - Envío manual funcionando contra provider real.

Carga estimada: 18-20 h.


SEMANA 18 (07/09 – 13/09) — Recordatorios inteligentes (parte 2)

Objetivos:
  - Job programado: priorización por riesgo.
  - Logs y métricas.

Entregable:
  - Recordatorios automáticos funcionando.
  - CU-03 — flujo principal completo.

Carga estimada: 18-20 h.


SEMANA 19 (14/09 – 20/09) — Overbooking inteligente

Objetivos:
  - Detección de slots de alto riesgo.
  - Configuración de tope por profesional.
  - Sugerencia + confirmación + ajuste de capacidad.

Entregable:
  - Overbooking funcionando con tope respetado.
  - CU-04 — diagrama de robustez y flujo principal.

Carga estimada: 18-20 h.


SEMANA 20 (21/09 – 27/09) — BUFFER 3 + cierre ICONIX restante

Objetivos:
  - Absorber retrasos.
  - Cerrar documentación de CU-03 y CU-04.
  - Refactor del módulo de inteligencia.

Entregable:
  - 4 casos de uso ICONIX con documentación completa.

Carga estimada: 12-15 h.


================================================================
FASE 4 — VALIDACIÓN CON PILOTO (28/09/2026 a 25/10/2026, 4 semanas)
================================================================


SEMANA 21 (28/09 – 04/10) — Preparación para piloto

Objetivos:
  - Pulido de UX en pantallas críticas (login, agenda, ficha).
  - Documento de onboarding paso a paso.
  - Carga manual de datos iniciales preparada.
  - Deploy de versión candidata.

Entregable:
  - Sistema desplegado y listo para piloto.

Carga estimada: 15-18 h.


SEMANA 22 (05/10 – 11/10) — Onboarding del piloto

Objetivos:
  - Identificar 1 clínica piloto en Rosario (vía red personal, colegio
    médico, contactos UAI). Búsqueda iniciada en septiembre.
  - Onboarding presencial: capacitación, carga inicial,
    configuración.
  - Notas de campo del proceso.

Entregable:
  - 1 piloto operando con datos reales.

Plan B si no hay piloto materializado al 15/10:
  - Ejecutar caso de uso simulado con datos sintéticos
    representativos.
  - Documentar supuestos explícitamente.
  - Defendible si se justifica la elección académicamente.

Carga estimada: 15-18 h.


SEMANA 23 (12/10 – 18/10) — Operación supervisada del piloto

Objetivos:
  - Acompañamiento al uso real.
  - Recolección de feedback estructurado.
  - Ajustes de bugs sin nuevas features.

Entregable:
  - Métricas reales del piloto.

Carga estimada: 15-18 h.


SEMANA 24 (19/10 – 25/10) — Análisis de resultados

Objetivos:
  - Comparar baseline vs operación con sistema.
  - Resultados cuantitativos y cualitativos.
  - Borrador del capítulo "Resultados" de la tesis.

Entregable:
  - Capítulo de resultados en borrador.

Carga estimada: 15-18 h.


================================================================
FASE 5 — REDACCIÓN FINAL Y DEFENSA (26/10/2026 a 31/12/2026,
9 semanas)
================================================================


SEMANAS 25-27 (26/10 – 15/11) — Redacción intensiva de tesis

Objetivos:
  - Completar puntos 2 a 7 del plan de negocios (guías UAI).
  - Marco teórico: FHIR, ICONIX, ML aplicado a salud, SaaS B2B.
  - Capítulo de metodología.
  - Capítulo de implementación.
  - Conclusiones y trabajo futuro.

Entregable:
  - Tesis en borrador completo.

Carga estimada: 18-20 h/semana (priorizar redacción sobre código).


SEMANAS 28-29 (16/11 – 29/11) — Revisión y ajuste

Objetivos:
  - Revisión con docentes.
  - Ajustes según feedback.
  - Bibliografía completa.
  - Diagramas finales.

Entregable:
  - Tesis lista para entrega.

Carga estimada: 15-18 h/semana.


SEMANAS 30-32 (30/11 – 31/12) — Holgura, demo, defensa

Objetivos:
  - Correcciones finales.
  - Preparación de defensa oral.
  - Demo en vivo del sistema.
  - Slides y material de apoyo.

Entregable:
  - Defensa aprobada.

Carga estimada: 10-15 h/semana (semanas más livianas).


================================================================
RESUMEN DE CARGA TOTAL
================================================================

Fase 1 (Cimientos):              9 semanas × ~14 h = ~125 h
Fase 2 (Historia + obra social): 5 semanas × ~14 h = ~70 h
Fase 3 (Inteligencia):           6 semanas × ~17 h = ~100 h
Fase 4 (Piloto):                 4 semanas × ~16 h = ~65 h
Fase 5 (Redacción y defensa):    8 semanas × ~16 h = ~130 h

TOTAL ESTIMADO:                                   ~490 horas

Encaja en tu disponibilidad real (480-640 h), con margen de 0-30%
según semanas concretas.


================================================================
SEGUIMIENTO Y AJUSTES
================================================================

Revisión semanal (15 minutos, viernes o domingo):
  - ¿Cumplí el objetivo de la semana?
  - Si no, ¿qué se posterga y a qué buffer?
  - ¿Hay tareas que migran a la siguiente semana?

Buffers planificados:
  - Semana 8 (08/07): absorción Fase 1.
  - Semana 14 (15/08): cierre ICONIX parcial.
  - Semana 20 (28/09): cierre ICONIX completo.
  - Semanas 30-32 (diciembre): holgura general.

Criterios de re-planificación si te atrasás más de 1 semana:

Orden de recorte (primero el que sale, último el que nunca sale):
  1. Overbooking (queda en roadmap, CU-04 se documenta sin
     implementar completo).
  2. Recordatorios automáticos diferenciados (queda solo el manual).
  3. Predicción heurística (queda como cálculo sin notificaciones).
  4. Obra social tope semanal (queda solo el Coverage simple).
  5. Historia clínica frontend (queda solo el backend).
  6. Agenda interactiva (queda lista simple).
  7. FHIR core nunca se recorta.
  8. Multitenant nunca se recorta.

Riesgos identificados y mitigación:

  - Trabajo intenso o viajes: usar buffers planificados.
  - Piloto no se materializa: plan B simulado activado el 15/10.
  - Complejidad de IA mayor a la esperada: heurística pura sin
    diferenciación si aplica.
  - Tesis escrita tarde: forzar 30 min de redacción diaria desde
    Fase 3 (no esperar a redacción intensiva).
  - Bloqueo técnico: usar IA asistente más agresivamente para
    desbloquear, pero validar el resultado.


================================================================
Versión 1.1 — 12/05/2026
Cambios respecto a 1.0:
  - Carga sincerada a 15-20 h/semana real.
  - Resumen LLM movido a roadmap.
  - 4 casos de uso ICONIX explicitados como entregables de tesis.
  - 3 buffers explícitos en lugar de uno solo al final.
  - Total estimado: ~490 horas (encaja en disponibilidad).
================================================================
