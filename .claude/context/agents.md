# Agentes disponibles — cuándo y cómo usarlos

## Equipo

| Agente | Cuándo activarlo | Cuándo NO usarlo |
|---|---|---|
| `backend-architect` | Diseño de schema multitenant, decisiones API, FHIR resource mapping, trade-offs Spring Boot | Implementación rutinaria de endpoints ya diseñados |
| `frontend-developer` | Componentes Angular 18, agenda visual, formularios FHIR, estado con Signals | Decisiones de arquitectura backend o ML |
| `ai-engineer` | Motor heurístico de no-show (MVP), recordatorios diferenciados por riesgo, overbooking inteligente. ML por tenant queda en roadmap. | Features que no involucran motor de inteligencia |
| `database-optimizer` | Schema multitenant, índices PostgreSQL, tuning de queries, migraciones Flyway | Diseño de features (usar `backend-architect` primero) |
| `security-engineer` | RBAC, JWT hardening, threat modeling, cumplimiento Ley 25.326, SMART on FHIR security | Implementación de features de negocio sin implicancias de seguridad |
| `software-architect` | Decisiones ICONIX (dominio, robustez, secuencia, clases), trade-offs de diseño sistémico | Implementación directa de código de producción |
| `iconix-expert` | Auditar y corregir artefactos ICONIX existentes: consistencia entre modelos, cumplimiento estricto del método, detección de fantasmas/redundancias/tipado, refutar hallazgos falsos | Diseñar el sistema desde cero (usar `software-architect`) o escribir código de producción |
| `multitenant-isolation-reviewer` | Auditar código de implementación (repos/JPA/services/controllers/Redis) para garantizar scoping por `tenant_id`, detectar fugas cross-tenant y anti-patrones de aislamiento | Diseño ICONIX (usar `iconix-expert`) o threat-modeling amplio (usar `security-engineer`) |
| `model-qa-specialist` | QA del motor heurístico (MVP): validación de factores, cobertura de casos, sesgos en las reglas. ML por tenant en roadmap. | Tareas que no involucran motor de inteligencia |
| `product-manager` | BMC, roadmap, PRDs, narrativa del plan de negocios para entregas académicas UAI | Decisiones técnicas de implementación |
| `api-tester` | Escribir tests JUnit 5 para nuevos endpoints FHIR, cobertura 95%+, tenant isolation | Tests E2E de frontend (usar Playwright directamente) |
| `test-results-analyzer` | Analizar reportes mvn test / ng test, cobertura, riesgo de regresión | Escribir tests nuevos |
| `technical-writer` | Documentar módulos en `backend/docs/`, actualizar `.claude/context/`, diagramas ICONIX | Código de producción |
| `code-reviewer` | Revisar PRs antes de merge a `develop` — checklist tenant_id, anti-patrones, seguridad | Auto-aprobar sin leer el diff completo |
| `reality-checker` | Validar entregables académicos antes de cada fecha de entrega (19/5, 16/6, 30/6) | Revisiones de código rutinarias |

---

## Flujo recomendado por tipo de tarea

### Feature nueva (endpoint FHIR o módulo de negocio)
1. `backend-architect` → diseño del recurso, schema multitenant y decisiones de API
2. `database-optimizer` → si requiere nuevas tablas o índices
3. `ai-engineer` → si el feature toca el motor de inteligencia (no-show, recordatorios, overbooking)
4. `api-tester` → tests del endpoint (TDD: tests antes que implementación)
5. `code-reviewer` → revisión del PR antes de merge a `develop`
6. `technical-writer` → documentación del módulo nuevo

### Feature frontend
1. `frontend-developer` → componente Angular 18, integración con JWT+cache
2. `api-tester` → si el componente llama endpoints nuevos
3. `code-reviewer` → revisión del PR

### Entrega académica (16/6, 30/6, 30/11, defensa diciembre)
1. `reality-checker` → validar que lo entregado cumple los puntos de la guía UAI
2. `software-architect` → generar diagramas ICONIX requeridos para los 4 CU core (dominio, robustez, secuencia, clases)
3. `technical-writer` → documentación técnica (OpenAPI, FHIR CapabilityStatement, backend/docs/, tesis)
4. `product-manager` → narrativa de negocio (BMC, plan de negocios, plan de trabajo)

### Bug o test fallando
1. `test-results-analyzer` → analizar el reporte y localizar el origen
2. (según área) `backend-architect` o `frontend-developer` → implementar el fix
3. `api-tester` → confirmar fix con test de regresión

### Revisión de seguridad (antes de cualquier release)
1. `security-engineer` → threat modeling, revisión JWT, RBAC, cumplimiento Ley 25.326
2. `code-reviewer` → checklist de seguridad y anti-patrones específicos del proyecto

---

## Notas de uso en Claude Code

- Activar agente: `/agent <nombre>` en Claude Code (ej. `/agent backend-architect`)
- Los agentes llevan prefijo de contexto del proyecto — no es necesario repetir el stack
- Si el agente está `🔄 in-progress` en INDEX.md → no tomar esa tarea sin coordinación
- Ante duda sobre qué agente usar: elegir el más específico; `backend-architect` es el default para backend
