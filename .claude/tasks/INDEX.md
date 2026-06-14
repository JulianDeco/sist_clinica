# Task Index — ClinicaSaaS

> **Protocolo para agentes**: la fuente de verdad del estado es
> `.claude/tasks/tasks.json` — leerlo PRIMERO en cada sesión y actualizarlo allí.
> Este índice es una vista navegable; mantenerlo sincronizado al cambiar tasks.json.
> Consultar `.claude/context/agents.md` para elegir el agente correcto.
> **Nunca trabajar en una tarea `en_progreso` de otro agente sin coordinación.**

---

## Estado global del proyecto

- **Stack**: Java 21 + Spring Boot 3 + Angular 18 (migrado 2026-06-08 — ADR-001/ADR-005)
- **Fase activa**: Fase 1 — Infra, Auth y Multitenant (+ entrega académica T-020)
- **Última actualización**: 2026-06-10
- **Scope vigente**: MVP del seminario (mayo–diciembre 2026)
- **Cronograma detallado**: `seminario/plan-de-trabajo.md`
- **Casos de uso ICONIX (4 core)**: `.claude/tasks/use-cases.md`
- **Plan de negocios**: `seminario/plan-de-negocios.md`

## Scope MVP vs Roadmap

Si se acumula más de 1 semana de retraso, recortar en este orden
(primero el que sale): overbooking → recordatorios diferenciados →
predicción heurística → obra social tope → historia frontend →
agenda interactiva. **Multitenant y FHIR core nunca se recortan.**

---

## Tablero de tareas (espejo de tasks.json)

### Fase 0 — Fundación

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-000 | Documentación de fundación e ingeniería (docs/, estándares, 11 ADRs, SDD) | ✅ hecho 2026-06-08 | software-architect | [ver](T-000.md) |

### Fase 1 — Infra, Auth y Multitenant

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-001 | Scaffold backend Spring Boot 3 / Java 21 (Maven, Flyway V001..V009, Docker) | 🧪 testeando | backend-architect | [ver](T-001.md) |
| T-002 | Scaffold frontend Angular 18 + Material (core/shared/features/layout, ADR-014) | 🔄 en_progreso | frontend-developer | [ver](T-002.md) |
| T-003 | Autenticación JWT — login, select-tenant, switch-tenant, logout (ADR-014) | ⬜ pendiente | — | [ver](T-003.md) |
| T-004 | RBAC dinámico — roles y permisos + cache Redis 5min | ⬜ pendiente | — | [ver](T-004.md) |

### Fase 2 — Core clínico FHIR

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-005 | FHIR R4 core: Patient, Practitioner, Coverage (JSONB, ADR-009/015) | ⬜ pendiente | — | [ver](T-005.md) |
| T-006 | Agenda: Slot, Schedule, Appointment — CU-01 backend | ⬜ pendiente | — | [ver](T-006.md) |
| T-007 | Historia clínica: Encounter + Observations SOAP — CU-02 backend | ⬜ pendiente | — | crear |

### Fase 3 — Motor de inteligencia

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-008 | Motor heurístico de no-show — CU-03 backend | ⬜ pendiente | — | crear |
| T-010 | Overbooking inteligente — CU-04 | ⬜ pendiente | — | crear |

### Fase 4 — Notificaciones

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-009 | Notificaciones diferenciadas por riesgo (Telegram + email MVP, ADR-013) | ⬜ pendiente | — | crear |

### Fase 5 — Frontend Angular (features)

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-011 | Login y shell Angular conectados al backend (depende T-002/T-003) | ⬜ pendiente | — | crear |
| T-012 | Agenda interactiva — CU-01 frontend | ⬜ pendiente | — | crear |
| T-013 | Consulta médica SOAP — CU-02 frontend | ⬜ pendiente | — | crear |
| T-014 | Dashboard de ausentismo | ⬜ pendiente | — | crear |

### Fase 6 — Despliegue y piloto

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-015 | Docker Compose + Nginx para el stack actual | ⬜ pendiente | — | crear |
| T-016 | Piloto real con clínica en Rosario (plan B: simulación) | ⬜ pendiente | — | crear |

### Integraciones LLM

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-017 | Resumen automático de historia clínica (Claude API) | ⬜ pendiente | — | crear |
| T-018 | Sugerencia automática CIE-10 (Claude API) | ⬜ pendiente | — | crear |

### Académico / Post-seminario

| ID | Tarea | Estado | Agente | Detalle |
|---|---|---|---|---|
| T-020 | Entrega académica ICONIX — diagramas, prototipo y patrones | 🧪 testeando | software-architect | [ver](T-020.md) |
| T-019 | Portal de pacientes (POST-SEMINARIO) | ⬜ pendiente | — | crear |

---

## Leyenda

⬜ pendiente · 🔄 en_progreso (anotar agente) · 🧪 testeando · ✅ hecho · ⛔ bloqueado
Estados válidos = los de `tasks.json`. "crear" en Detalle = el `T-XXX.md` aún no existe.
