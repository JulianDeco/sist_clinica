# ClinicaSaaS

Plataforma de gestión clínica por suscripción para consultorios y clínicas médicas privadas de 1 a 5 profesionales en Argentina, con foco inicial en Rosario.

Los datos clínicos se almacenan sobre el estándar internacional **HL7 FHIR R4**, garantizando portabilidad e interoperabilidad con obras sociales, laboratorios y otros sistemas de salud presentes y futuros.

## El problema

El ausentismo de pacientes afecta entre el **23 % y el 30 % de los turnos** en Argentina — un costo operativo crónico sin solución tecnológica adoptada en el segmento de clínicas pequeñas. Las herramientas disponibles cubren gestión básica de agenda pero no predicen ni mitigan el problema.

## Qué resuelve el MVP

| Capacidad | Descripción |
|---|---|
| **Predicción explicable de ausentismo** | Estima la probabilidad de no-show por paciente y explica los factores (historial, anticipación, franja horaria). Heurística en MVP, diseñada para evolucionar a ML por tenant. |
| **Recordatorios inteligentes** | Priorización de notificaciones según riesgo calculado — reduce costo y aumenta tasa de respuesta efectiva. |
| **Overbooking inteligente** | Sugiere sobreturnos en slots de alto riesgo con tope parametrizable por el profesional. |
| **Resumen clínico con IA** | Genera resumen del encuentro SOAP en lenguaje natural a partir de datos FHIR. El profesional revisa antes de guardar. |
| **Sugerencia CIE-10** | Sugiere código de diagnóstico desde texto libre. El profesional conserva el criterio final. |
| **Obra social con tope semanal** | Validación de cobertura y tope por paciente integrada al flujo de reserva. |
| **Agenda interactiva FHIR** | Ficha de paciente, historia clínica SOAP e interoperabilidad FHIR R4 nativa. |

## Stack técnico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA |
| Base de datos | PostgreSQL 16 (FHIR como JSONB + tablas relacionales) |
| Caché / sesiones | Redis 8 |
| Frontend | Angular 18 · Angular Material |
| Infraestructura | Docker · Compose · Nginx |
| IA generativa | Claude API (Anthropic) |

## Casos de uso core (ICONIX)

| CU | Descripción |
|---|---|
| CU-01 | Reservar turno con validación integral (slot, obra social, riesgo, overbooking) |
| CU-02 | Gestionar consulta médica — registro SOAP, Encounter FHIR, cierre |
| CU-03 | Calcular y notificar predicción de ausentismo |
| CU-04 | Sugerir y aplicar overbooking inteligente |

## Documentación

- [Arquitectura](arquitectura/01-high-level-architecture.md) — Capas, despliegue, seguridad
- [Diagramas ICONIX](diagramas/index.md) — Casos de uso, dominio, robustez, secuencia, clases, DER
- [Estándares de ingeniería](estandares/01-backend-standards.md) — Backend, frontend, DB, seguridad, API, Git
- [ADRs](adr/decisions-log.md) — 16 decisiones arquitectónicas registradas
- [Propuesta de valor](seminario/propuesta-de-valor.md) — Contexto de mercado y modelo de negocio
- [Roadmap](seminario/roadmap.md) — Cronograma mayo–diciembre 2026
