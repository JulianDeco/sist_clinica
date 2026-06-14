# CLAUDE.md — ClinicaSaaS (Sistema Médico FHIR)

## Contexto del proyecto

ClinicaSaaS es un SaaS multitenant FHIR R4 para clínicas pequeñas
(1–5 profesionales) en Argentina, con foco inicial en Rosario.
Proyecto de tesis del Seminario de Trabajo Final UAI 2026.

**Stack actual**: Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA ·
PostgreSQL 16 · Redis 8 · Angular 18 · Angular Material · Docker / Compose.
Stack anterior (FastAPI + Next.js) descartado el 2026-06-08. Ver ADR-001 y ADR-005.

- **Plan de negocios y mercado**: `seminario/plan-de-negocios.md`
- **Plan de trabajo mayo–diciembre 2026**: `seminario/plan-de-trabajo.md`
- **Casos de uso ICONIX (núcleo académico)**: `.claude/tasks/use-cases.md`
- **Índice general del proyecto**: `INDEX.md`
- **Documentación de ingeniería completa**: `docs/` ← leer antes de codificar

## Scope MVP del seminario (mayo–diciembre 2026)

**Carga real disponible**: 15–20 h/semana × 32 semanas ≈ 480–640 h.
**Total estimado del MVP**: ~490 h.

**Dentro del MVP**:
- FHIR R4 multitenant (Patient, Practitioner, Appointment, Encounter,
  Observation, Coverage).
- Agenda interactiva, ficha de paciente, historia clínica SOAP básica.
- Obra social con validación de tope semanal.
- Predicción heurística explicable de ausentismo.
- Recordatorios inteligentes diferenciados por riesgo.
- Overbooking inteligente.
- Resumen automático de historia clínica con LLM y sugerencia CIE-10
  (Claude API — ADR-012, T-017/T-018, incorporados al MVP el 2026-06-08).
- 1 piloto real (o plan B simulado).

**En roadmap post-MVP (NO se implementan en el seminario)**:
cobertura óptima automatizada,
ML entrenado por tenant, importador masivo, alta automática de tenant,
marketplace B2C.

**4 casos de uso core ICONIX**:
- CU-01: Reservar turno con validación integral.
- CU-02: Gestionar consulta médica (SOAP básico).
- CU-03: Calcular y notificar predicción de ausentismo.
- CU-04: Sugerir y aplicar overbooking inteligente.

Si se acumula más de 1 semana de atraso, recortar en este orden:
resumen LLM y sugerencia CIE-10 (T-017/T-018) → overbooking →
recordatorios diferenciados → predicción heurística →
obra social tope semanal → historia frontend → agenda interactiva.
Multitenant y FHIR core nunca se recortan.

## Estructura del repositorio

```
sist_clinica/
├── src/
│   ├── backend/      Spring Boot 3 / Java 21 — Clean Architecture
│   │   └── src/main/java/com/clinicasaas/
│   │       ├── config/       Spring @Configuration classes
│   │       ├── domain/       Entities, value objects, repository interfaces
│   │       ├── application/  Use cases (UC-01..04)
│   │       ├── infrastructure/ JPA, Redis, notifications adapters
│   │       └── api/          Controllers, DTOs, exception handlers
│   └── frontend/     Angular 18 + Angular Material
│       └── src/app/
│           ├── core/         Auth, interceptors, guards, API clients
│           ├── shared/       Reusable components, directives, pipes
│           ├── features/     Lazy-loaded feature modules
│           └── layout/       Shell components
├── docs/             Documentación de ingeniería
│   ├── architecture/ Diagramas y descripción de arquitectura
│   ├── standards/    Estándares: backend, frontend, DB, Redis, seguridad, API, Git
│   ├── testing/      Estrategia de testing + SDD
│   ├── modules/      Specs por módulo (specs/ dentro de cada uno)
│   └── adr/          Architecture Decision Records
├── seminario/        Documentación académica (plan de negocios, entregas, PDFs)
├── demos/
│   └── noshow_demo/  Prototipo Streamlit de predicción (referencia, no producción)
├── .claude/          Contexto, tareas y agentes
├── INDEX.md          Índice navegable del proyecto
└── CLAUDE.md         Este archivo
```

## Arquitectura

| Capa | Responsabilidad | Ubicación |
|---|---|---|
| API Layer | Controllers, DTOs, request/response mapping | `src/backend/.../api/` |
| Application Layer | Use cases (UC-01..04), orquestación, @Transactional | `src/backend/.../application/` |
| Domain Layer | Entities JPA, value objects, repository interfaces | `src/backend/.../domain/` |
| Infrastructure | JPA repos, Redis adapters, notification clients | `src/backend/.../infrastructure/` |
| Config | SecurityConfig, RedisConfig, JpaConfig, OpenApiConfig | `src/backend/.../config/` |
| FHIR API | Endpoints `/fhir/R4/*`, recursos JSONB | `src/backend/.../api/fhir/` |
| Auth | JWT stateless, BCrypt, refresh token rotation | `src/backend/.../config/SecurityConfig.java` |
| RBAC | Roles en JWT, permisos en Redis cache 5min | `src/backend/.../infrastructure/cache/` |
| Motor IA | Heurística no-show, reglas overbooking | `src/backend/.../application/intelligence/` |
| Notificaciones | Telegram / email según riesgo (ADR-013; WhatsApp/SMS post-piloto) | `src/backend/.../infrastructure/notifications/` |
| Frontend | Angular 18, Signals state, Material components | `src/frontend/src/app/` |

## Workflow obligatorio por feature

**Metodología**: SDD · TDD · Clean Architecture · DDD · ADR
**NUNCA empezar a codificar sin un spec aprobado. NUNCA adivinar reglas de negocio.**

```
1. Specification      → docs/specifications/{Feature}.spec.md  ← APPROVED antes de continuar
2. ADR impact         → ¿decisión arquitectónica nueva? → escribir ADR antes del dominio
3. Domain design      → DDD: entidades, value objects, agregados, eventos
4. Test design        → TC-XX derivados de AC-XX del spec
5. Test implementation→ escribir tests fallando (Red) — mvn test confirma que fallan
6. Production code    → mínimo para pasar tests (Green) → refactor → dominio→app→infra→API
7. Documentation      → spec IMPLEMENTED, JavaDoc, module doc, ADR si aplica
8. Review             → PR → develop, DoD checklist, aprobación @julian
```

**Definition of Ready** (antes de Step 6): spec aprobado · AC definidos · edge cases listados · ADR evaluado
**Definition of Done** (antes de merge): tests passing · coverage ok · JavaDoc · spec IMPLEMENTED · tasks.json hecho

## Gestión de tareas

Las tareas se almacenan en `.claude/tasks/tasks.json`.

Estados válidos: `pendiente` | `en_progreso` | `testeando` | `bloqueado` | `hecho`

Cada tarea tiene: `id`, `titulo`, `estado`, `agente`, `prioridad` (alta/media/baja),
`fecha_inicio`, `fecha_fin`, `descripcion` y `ref` (link al .md de detalle si existe).

Al comenzar una tarea: cambiar `estado` a `en_progreso` y asignar `agente`.
Al terminar: `estado` → `testeando`, luego `hecho` con `fecha_fin`.
Si se bloquea: `estado` → `bloqueado` + nota en `descripcion`.
Tareas nuevas: agregar entrada en `tasks.json` con id correlativo.

## Protocolo multi-agente

1. Leer `.claude/tasks/tasks.json` primero — es la fuente de verdad del estado
2. Consultar `.claude/context/agents.md` para elegir el agente correcto
3. Reclamar tarea: `estado` → `en_progreso` + campo `agente`
4. No trabajar en tareas `en_progreso` de otro agente
5. Al terminar: `estado` → `testeando` → `hecho` con `fecha_fin`
6. Si bloqueado: `estado` → `bloqueado` + motivo en `descripcion`
7. PRs a `main` requieren aprobación de @julian

**Agentes disponibles** (activar con `/agent <nombre>` en Claude Code):
`backend-architect` · `frontend-developer` · `ai-engineer` · `database-optimizer` · `security-engineer` · `software-architect` · `model-qa-specialist` · `product-manager` · `api-tester` · `test-results-analyzer` · `technical-writer` · `code-reviewer` · `reality-checker`

## Git Flow

```
main ← release/* o hotfix/* (aprobación @julian)
develop ← features integradas
feature/T-XXX-nombre ← una branch por tarea
```

## Routing — qué leer según la tarea

| Tarea | Sub-contexto |
|---|---|
| **Estado de tareas** | `.claude/tasks/tasks.json` ← leer primero siempre |
| **Plan de trabajo cronograma** | `seminario/plan-de-trabajo.md` |
| **Plan de negocios y mercado** | `seminario/plan-de-negocios.md` |
| **Agentes — cuándo y cómo usar cada uno** | `.claude/context/agents.md` |
| **Casos de uso ICONIX (4 core)** | `.claude/tasks/use-cases.md` |
| **Arquitectura general** | `docs/architecture/01-high-level-architecture.md` |
| **Estándares backend Java/Spring** | `docs/standards/01-backend-standards.md` |
| **Estándares frontend Angular** | `docs/standards/02-frontend-standards.md` |
| **Estándares BD / migraciones** | `docs/standards/03-database-standards.md` |
| **Estándares Redis** | `docs/standards/04-redis-standards.md` |
| **Estándares seguridad** | `docs/standards/05-security-standards.md` |
| **Estándares API REST** | `docs/standards/06-api-standards.md` |
| **Testing / SDD** | `docs/testing/01-testing-standards.md` |
| **Documentación JavaDoc** | `docs/standards/07-documentation-standards.md` |
| **Git / commits / PRs** | `docs/standards/08-git-standards.md` |
| **Workflow de feature (SDD+TDD+DDD)** | `docs/standards/09-feature-workflow.md` |
| **Reglas agente IA** | `docs/standards/10-ai-agent-rules.md` |
| **ADRs (decisiones)** | `docs/adr/README.md` |
| **Specs de features** | `docs/specifications/` — template: `_template.spec.md` |
| Auth / JWT / OAuth2 | `.claude/context/modules/01-auth.md` |
| RBAC / roles / permisos | `.claude/context/modules/02-rbac.md` |
| FHIR Resources | `.claude/context/modules/03-fhir.md` |
| Agenda / turnos / sobreturnos | `.claude/context/modules/04-agenda.md` |
| Historial clínico / SOAP | `.claude/context/modules/05-historial.md` |
| Obra social / tope semanal | `.claude/context/modules/06-obra-social.md` |
| Frontend componentes / estado | `.claude/context/modules/07-frontend.md` |
| BD / migraciones / modelos | `.claude/context/modules/08-database.md` |
| Motor IA (heurística no-show, overbooking) | `.claude/context/modules/09-intelligence.md` |
| Notificaciones inteligentes | `.claude/context/modules/10-notifications.md` |
| Stack / versiones | `.claude/context/00-stack.md` |
| Docker / Nginx / env vars | `.claude/context/00-infra.md` |

## Convenciones

- Archivos Java: `PascalCase.java` — packages en `snake_case` (Java convention)
- Archivos Angular: `kebab-case.component.ts`, `PascalCase` para clases
- Endpoints FHIR: `/fhir/R4/{ResourceType}` — siempre mayúscula inicial
- Módulos backend: `{Module}Controller.java` + `{Module}Repository.java` + `{Module}UseCaseImpl.java`
- Componentes Angular: máximo 150 líneas — extraer subcomponentes si supera
- Flujo por función: spec → test → implementar → commit (nunca acumular)
- Documentar toda clase y método público: JavaDoc obligatorio
- Toda decisión no obvia: comentario inline `// WHY:` o ADR

## Anti-patrones

| Anti-patrón | Regla |
|---|---|
| Permisos completos en JWT | Usar híbrido: `role` en JWT + permisos en Redis cache 5min |
| `spring.jpa.hibernate.ddl-auto=create/update` | Flyway es el único mecanismo de migraciones |
| Lógica de negocio en Controllers | Todo en Application Service / Use Case |
| `@Autowired` en Domain Entities | Las entidades no dependen de Spring beans |
| Consulta sin `tenant_id` en repository | Toda query de datos tenant-scoped incluye tenantId |
| `git add .` | Siempre especificar archivos — nunca add masivo |
| Implementar sin spec aprobado | SDD: spec primero, código después |
| Código sin JavaDoc en clases/métodos públicos | Documentar siempre al crear |
| Archivos > 200 líneas | Dividir en submódulos |

## Skills superpowers

Invocar con `Skill("superpowers:<nombre>")`:

| Cuándo | Skill |
|---|---|
| Inicio de cualquier conversación | `using-superpowers` |
| Antes de feature / componente / cambio de comportamiento | `brainstorming` |
| Con spec aprobada, antes de tocar código | `writing-plans` |
| Ejecutar plan en sesión actual (tareas independientes) | `subagent-driven-development` |
| Ejecutar plan en sesión nueva con checkpoints | `executing-plans` |
| 2+ tareas independientes sin estado compartido | `dispatching-parallel-agents` |
| Antes de escribir código de implementación | `test-driven-development` |
| Feature que necesita aislamiento del workspace | `using-git-worktrees` |
| Ante cualquier bug o test fallando | `systematic-debugging` |
| Antes de afirmar que algo está listo o hacer commit | `verification-before-completion` |
| Al completar feature o antes de mergear | `requesting-code-review` |
| Al recibir feedback de revisión | `receiving-code-review` |
| Implementación completa, decidir cómo integrar | `finishing-a-development-branch` |
| Crear o modificar skills | `writing-skills` |

## Reglas

- Al crear módulo nuevo → crear `docs/modules/XX-nombre.md` Y actualizar Routing ANTES de codificar
- Al surgir tarea nueva → agregarla a `.claude/tasks/INDEX.md` + crear `T-XXX.md`
- Al tomar una decisión arquitectónica → crear ADR en `docs/adr/`
- Fuente de verdad: **PostgreSQL** (fhir_resources JSONB + tablas relacionales)
- Migraciones: siempre Flyway `V{N}__description.sql` — nunca `ddl-auto=create`
- Tests: `mvn test` (backend) y `ng test` (frontend) antes de cada commit
- Conventional Commits: `feat|fix|test|docs|refactor|chore|spec|adr(scope): mensaje`
- PRs siempre a `develop`; PRs a `main` requieren aprobación de @julian
- Nunca `git add .` — especificar archivos; nunca force push a `develop` ni `main`
- Spec file obligatorio y aprobado antes de cualquier implementación
