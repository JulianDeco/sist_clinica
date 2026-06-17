# Kuris

Plataforma SaaS multitenant de gestión clínica con interoperabilidad FHIR R4
e inteligencia operacional para clínicas pequeñas (1–5 profesionales) en Argentina.

Proyecto de Tesis — Seminario de Trabajo Final UAI 2026 — Julián Decoppet

---

## ¿Qué es este sistema?

Kuris permite a clínicas pequeñas gestionar turnos, consultas médicas e historias
clínicas desde un único sistema, con soporte para múltiples obras sociales, predicción
heurística de ausentismo de pacientes y overbooking inteligente.

El diseño sigue el estándar internacional **FHIR R4** (Fast Healthcare Interoperability
Resources), lo que permite interoperar con otros sistemas de salud. La arquitectura es
**multitenant**: una sola instancia del sistema sirve a múltiples clínicas, con datos
completamente aislados entre ellas.

---

## Stack

| Capa | Tecnología | Por qué |
|---|---|---|
| Backend | Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA | Ecosistema maduro para APIs FHIR; DI nativa facilita Clean Architecture |
| Base de datos | PostgreSQL 16 · Flyway (migraciones) | JSONB para recursos FHIR; row-level multitenancy sin overhead de esquemas |
| Caché / estado efímero | Redis 8 | Scores de riesgo de corta vida; permisos RBAC sin consultas repetidas |
| Frontend | Angular 18 · Angular Material | Signals para estado reactivo; componentes accesibles listos |
| Infraestructura | Docker · Docker Compose · Nginx | Reproducibilidad local y en VPS; reverse proxy sin configuración extra |

Para el razonamiento detrás de cada elección tecnológica, ver los [ADRs](docs/adr/README.md).

---

## Arquitectura en una línea

```
Angular 18 → Spring Boot API (Clean Architecture) → PostgreSQL 16
                    ↕ Redis 8 (cache)   ↕ Claude API (LLM)
```

El backend sigue Clean Architecture: las reglas de negocio viven en `domain/` y
`application/`, sin dependencias hacia frameworks. Los detalles (JPA, Redis, HTTP)
están en `infrastructure/` y `api/`. Ver [arquitectura general](docs/architecture/01-high-level-architecture.md).

---

## Documentación de ingeniería

### ADRs — decisiones arquitectónicas

Los **Architecture Decision Records** documentan por qué se tomó cada decisión de diseño
significativa: el contexto, las alternativas consideradas y los trade-offs aceptados.
Son inmutables una vez aceptados — si una decisión cambia, se crea un ADR nuevo que
referencia al anterior. Sirven para no repetir debates ya resueltos y para que cualquier
colaborador nuevo entienda el porqué del stack sin leer todo el historial de git.

→ [Índice de ADRs](docs/adr/README.md)

### Estándares de desarrollo

Convenciones obligatorias para mantener consistencia a lo largo del proyecto. Cada
estándar cubre patrones correctos, anti-patrones prohibidos y ejemplos concretos.

| Estándar | Qué cubre |
|---|---|
| [Backend Java/Spring](docs/standards/01-backend-standards.md) | Estructura de paquetes, uso de anotaciones, manejo de errores, transacciones |
| [Frontend Angular](docs/standards/02-frontend-standards.md) | Estructura de módulos, uso de Signals, naming de componentes |
| [Base de datos / migraciones](docs/standards/03-database-standards.md) | Convenciones SQL, uso de Flyway, índices, multitenancy |
| [Redis](docs/standards/04-redis-standards.md) | Formato de claves (`clinica:*`), TTLs, qué va y qué no va en caché |
| [Seguridad](docs/standards/05-security-standards.md) | JWT, BCrypt, RBAC, headers HTTP, validación de entrada |
| [API REST](docs/standards/06-api-standards.md) | Versionado, formatos de respuesta, códigos de estado, paginación |
| [Testing](docs/testing/01-testing-standards.md) | Pirámide de tests, umbrales de cobertura, mocks vs. integración real |
| [Documentación (JavaDoc)](docs/standards/07-documentation-standards.md) | Qué documentar, formato de JavaDoc, reglas de comentarios inline |
| [Git / commits / PRs](docs/standards/08-git-standards.md) | Conventional Commits, Git Flow, reglas de merge |
| [Workflow de feature](docs/standards/09-feature-workflow.md) | El ciclo obligatorio: Spec → ADR → Domain → Test → Código → Docs → Review |
| [Reglas agente IA](docs/standards/10-ai-agent-rules.md) | Qué puede y no puede decidir un agente de forma autónoma |

### Especificaciones activas

Las specs documentan el comportamiento esperado de una feature **antes** de escribir
código. Incluyen criterios de aceptación, casos borde y el contrato de la API. Ninguna
feature se implementa sin spec aprobada.

- [T-002 — Scaffold Frontend Angular 18](docs/specifications/T-002-scaffold-frontend.spec.md)
- [T-003 — Auth JWT](docs/specifications/T-003-auth-jwt.spec.md)
- [DB Schema](docs/specifications/DB-Schema.spec.md)

---

## Documentación académica (Seminario UAI)

| Documento | Qué contiene |
|---|---|
| [Plan de negocios](seminario/plan-de-negocios.md) | Propuesta de valor, segmento de mercado, modelo de revenue, competencia |
| [Plan de trabajo](seminario/plan-de-trabajo.md) | Cronograma técnico mayo–diciembre 2026, hitos y horas estimadas |
| [Business Model Canvas](seminario/bmc.md) | Canvas de nueve bloques |
| [Casos de uso ICONIX](seminario/iconix/02-especificacion-casos-de-uso.md) | CU-01..CU-04 con cursos básicos y alternos |
| [Diagramas ICONIX](seminario/iconix/diagramas/) | Casos de uso · Dominio · Robustez · Secuencia · Clases |

---

## Gestión de tareas

Las tareas se rastrean en [`.claude/tasks/tasks.json`](.claude/tasks/tasks.json), que es
la **fuente de verdad** del estado actual del proyecto. Cada tarea tiene un archivo de
detalle en `.claude/tasks/` y referencia la spec correspondiente en `docs/specifications/`.

---

## Estructura del repositorio

```
sist_clinica/
├── src/
│   ├── backend/      Spring Boot 3 / Java 21 — Clean Architecture
│   │   └── src/main/java/com/kuris/
│   │       ├── api/            Controllers, DTOs, exception handlers
│   │       ├── application/    Casos de uso (UC-01..04), lógica de negocio
│   │       ├── domain/         Entidades JPA, repositorios (interfaces)
│   │       ├── infrastructure/ JPA repos, Redis, notificaciones (implementaciones)
│   │       └── config/         Spring @Configuration (Security, Redis, OpenAPI)
│   └── frontend/     Angular 18 + Angular Material
│       └── src/app/
│           ├── core/           Auth, interceptors, guards, clientes API
│           ├── features/       Módulos lazy-loaded por feature
│           ├── shared/         Componentes, directivas y pipes reutilizables
│           └── layout/         Shell de la aplicación
├── docs/             Documentación de ingeniería (estándares, ADRs, specs)
├── seminario/        Documentación académica (plan de negocios, ICONIX, PDFs)
├── demos/            Prototipo Streamlit de predicción de ausentismo (referencia)
└── .claude/          Contexto, tareas y definición de agentes
```

---

*Última actualización: 11/06/2026*
