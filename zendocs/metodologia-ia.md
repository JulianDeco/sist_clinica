# Metodología de desarrollo con IA

Kuris utiliza un agente de IA (Claude, de Anthropic) como asistente de desarrollo dentro de un proceso de ingeniería estructurado. Esta página describe cómo se controla ese proceso y qué garantías ofrece sobre la calidad del producto.

## El agente no decide — asiste

El agente no tiene autonomía sobre el producto. Toda decisión de diseño, arquitectura o comportamiento del sistema es tomada por el autor del proyecto. El agente ejecuta tareas específicas bajo instrucciones explícitas y dentro de un flujo de trabajo que impone controles en cada etapa.

## Workflow obligatorio por feature

Ninguna línea de código de producción se escribe sin pasar por estas etapas en orden:

| Paso | Actividad | Control |
|---|---|---|
| 1 | **Specification** — spec aprobado antes de continuar | El autor aprueba el spec explícitamente |
| 2 | **ADR impact** — ¿hay decisión arquitectónica nueva? | Se documenta un ADR antes de tocar el dominio |
| 3 | **Domain design** — entidades, value objects, agregados | Revisado por el autor antes de avanzar |
| 4 | **Test design** — casos de prueba derivados de los criterios de aceptación | Stubs creados, tests en rojo antes del código |
| 5 | **TDD Red** — tests con aserción real, clase de producción inexistente | `mvn test` debe fallar — si pasa, el test está mal |
| 6 | **TDD Green** — código mínimo para que los tests pasen | `mvn test` debe pasar, nada más |
| 7 | **TDD Refactor** — limpiar sin cambiar comportamiento | Tests siguen en verde |
| 8 | **Documentation** — JavaDoc, spec marcado como IMPLEMENTED | Obligatorio antes del PR |
| 9 | **Review** — PR a `develop`, checklist DoD, aprobación del autor | Sin aprobación no hay merge |

**El agente no puede saltear pasos.** Si intenta codificar sin spec aprobado, se lo detiene.

## Control sobre decisiones arquitectónicas

Cada decisión no obvia queda registrada como un **Architecture Decision Record (ADR)**. El proyecto cuenta con 16 ADRs que documentan por qué se eligió cada tecnología y qué alternativas se descartaron.

Los ADRs son inmutables una vez aprobados — si una decisión cambia, se escribe un nuevo ADR que referencia al anterior. Esto crea una trazabilidad completa de la evolución arquitectónica del sistema.

→ Ver [Log de decisiones](adr/decisions-log/) y los [16 ADRs](adr/ADR-001-spring-boot/) del proyecto.

## Control sobre el código generado

- **Nunca `git add .`** — cada archivo commiteado se especifica explícitamente.
- **Nunca force push** a `develop` ni a `main`.
- **PRs a `main` requieren aprobación del autor** — el agente no puede integrar a producción por su cuenta.
- **Commits convencionales** — cada commit tiene tipo, scope y mensaje que describen el cambio. El historial es auditable.
- **Tests antes de cada commit** — `mvn test` (backend) y `ng test` (frontend) deben pasar.

## Control sobre el alcance

El agente opera con un **scope explícito del MVP** definido en el plan de trabajo. Tiene prohibido implementar funcionalidades fuera del MVP sin instrucción del autor. Si detecta que algo está fuera del alcance acordado, lo señala en lugar de implementarlo.

El criterio de recorte también está documentado — si el proyecto se atrasa, se sabe exactamente qué se corta primero y qué nunca se corta.

→ Ver [Roadmap y criterio de recorte](seminario/roadmap/).

## Metodología de testing

El proyecto sigue **TDD estricto**: la clase de producción no existe cuando se escribe el test. Si el test pasa en la fase Red, el test está mal escrito — se corrige antes de continuar.

La cobertura objetivo es 95 %+ en endpoints críticos (autenticación, turnos, historia clínica). Los tests de integración usan base de datos real — no mocks — para evitar divergencias entre el comportamiento testeado y el comportamiento en producción.

→ Ver [Estándares de testing](testing/01-testing-standards/).

## Agentes especializados

El proyecto define un equipo de agentes con roles específicos. Cada agente tiene un dominio de responsabilidad y restricciones explícitas sobre qué no puede hacer:

| Agente | Responsabilidad | Restricción |
|---|---|---|
| `backend-architect` | Diseño de schema, decisiones API, FHIR mapping | No implementa código rutinario |
| `security-engineer` | RBAC, JWT, cumplimiento Ley 25.326 | No toma decisiones de negocio |
| `database-optimizer` | Schema multitenant, índices, migraciones Flyway | No diseña features (eso es `backend-architect`) |
| `ai-engineer` | Motor heurístico de no-show, overbooking, recordatorios | No toca features sin motor de inteligencia |
| `code-reviewer` | Revisión de PRs antes de merge | No auto-aprueba sin leer el diff completo |
| `reality-checker` | Valida entregables académicos antes de cada entrega UAI | No modifica código |

Ningún agente trabaja en tareas que otro agente tenga en progreso. El estado de cada tarea se registra en un archivo centralizado de gestión (`tasks.json`).

## En resumen

El uso de IA en este proyecto no reemplaza el criterio de ingeniería — lo asiste. El autor diseña, aprueba, revisa y decide. El agente ejecuta dentro de un marco metodológico que garantiza trazabilidad, cobertura de tests y control sobre cada cambio que llega a producción.
