# ClinicaSaaS

Plataforma SaaS multitenant de gestión clínica con interoperabilidad FHIR R4
e inteligencia operacional para clínicas pequeñas (1–5 profesionales) en Argentina.

Proyecto de Tesis — Seminario de Trabajo Final UAI 2026 — Julián Decoppet

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA |
| Base de datos | PostgreSQL 16 (row-level multitenancy, Flyway) |
| Caché / estado efímero | Redis 8 |
| Frontend | Angular 18 · Angular Material |
| Infraestructura | Docker · Docker Compose · Nginx |

---

## Documentación de ingeniería

- [Índice completo de docs](docs/README.md)
- [Arquitectura general](docs/architecture/01-high-level-architecture.md)
- [ADRs — decisiones arquitectónicas](docs/adr/README.md)

### Estándares

- [Backend Java/Spring](docs/standards/01-backend-standards.md)
- [Frontend Angular](docs/standards/02-frontend-standards.md)
- [Base de datos / migraciones](docs/standards/03-database-standards.md)
- [Redis](docs/standards/04-redis-standards.md)
- [Seguridad](docs/standards/05-security-standards.md)
- [API REST](docs/standards/06-api-standards.md)
- [Testing](docs/testing/01-testing-standards.md)
- [Documentación (JavaDoc)](docs/standards/07-documentation-standards.md)
- [Git / commits / PRs](docs/standards/08-git-standards.md)
- [Workflow de feature (SDD+TDD+DDD)](docs/standards/09-feature-workflow.md)
- [Reglas agente IA](docs/standards/10-ai-agent-rules.md)

### Especificaciones activas

- [T-002 — Scaffold Frontend Angular 18](docs/specifications/T-002-scaffold-frontend.spec.md)
- [DB Schema](docs/specifications/DB-Schema.spec.md)

---

## Documentación académica

- [Plan de negocios](seminario/plan-de-negocios.md)
- [Plan de trabajo mayo–diciembre 2026](seminario/plan-de-trabajo.md)
- [Business Model Canvas](seminario/bmc.md)

---

## Gestión de tareas

- [Estado de tareas (fuente de verdad)](.claude/tasks/tasks.json)
- [Casos de uso ICONIX](.claude/tasks/use-cases.md)

---

## Estructura del repositorio

```
sist_clinica/
├── src/
│   ├── backend/      Spring Boot 3 / Java 21
│   └── frontend/     Angular 18 + Angular Material
├── docs/             Documentación de ingeniería
│   ├── architecture/
│   ├── standards/
│   ├── testing/
│   ├── specifications/
│   └── adr/
├── seminario/        Documentación académica
├── demos/            Prototipos de referencia
└── .claude/          Contexto, tareas y agentes
```

---

*Última actualización: 09/06/2026*
