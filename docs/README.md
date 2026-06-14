# ClinicaSaaS — Documentación de ingeniería

Esta carpeta contiene toda la documentación técnica del proyecto. Su propósito es que
cualquier colaborador (o agente) pueda entender las decisiones de diseño y las
convenciones del proyecto sin leer el código fuente primero.

---

## Organización

### `architecture/` — Diseño del sistema

Diagramas y descripción de la arquitectura general: cómo interactúan los componentes,
el flujo de datos entre capas, y el mapa de integraciones externas. Es el punto de
partida para entender cómo está organizado el sistema en su conjunto.

→ [Arquitectura general](architecture/01-high-level-architecture.md)

---

### `adr/` — Architecture Decision Records

Un ADR es un documento corto que responde tres preguntas sobre una decisión técnica
significativa: ¿cuál era el problema?, ¿qué se decidió y por qué?, ¿qué alternativas
se descartaron y con qué trade-offs?

Los ADRs son **inmutables** una vez aceptados. Si una decisión cambia (por ejemplo,
migrar de FastAPI a Spring Boot, como sucedió en junio 2026), se crea un ADR nuevo
que referencia y supersede al anterior. Esto preserva la historia completa del porqué
del sistema.

**Cuándo leer los ADRs:** antes de cambiar una elección tecnológica, integrar un
componente nuevo, o cuando algo del stack te resulte no obvio.

→ [Índice de ADRs](adr/README.md)

---

### `standards/` — Convenciones de desarrollo

Los estándares definen **cómo** se escribe el código en este proyecto. No son
sugerencias: son las reglas que garantizan que el código de distintas sesiones o
agentes sea consistente, seguro y mantenible.

Cada documento de estándar tiene la misma estructura: contexto, reglas positivas
(qué hacer), anti-patrones (qué no hacer) y ejemplos.

| Archivo | Qué cubre |
|---|---|
| [01-backend-standards.md](standards/01-backend-standards.md) | Estructura de paquetes Clean Architecture, anotaciones Spring, manejo de errores, transacciones `@Transactional` |
| [02-frontend-standards.md](standards/02-frontend-standards.md) | Módulos Angular, Signals para estado, naming de componentes, límite de 150 líneas por componente |
| [03-database-standards.md](standards/03-database-standards.md) | Convenciones SQL, Flyway como único mecanismo de migración, índices, `tenant_id` en toda query |
| [04-redis-standards.md](standards/04-redis-standards.md) | Formato de claves `clinica:{tenant}:{recurso}:{id}`, TTLs por tipo de dato, qué nunca persistir en Redis |
| [05-security-standards.md](standards/05-security-standards.md) | JWT stateless + refresh rotation, BCrypt, RBAC híbrido (rol en token + permisos en Redis), headers HTTP |
| [06-api-standards.md](standards/06-api-standards.md) | Versionado `/api/v1/`, formato de respuesta, códigos HTTP, paginación, errores estructurados |
| [07-documentation-standards.md](standards/07-documentation-standards.md) | JavaDoc obligatorio en toda clase y método público, regla del comentario `// WHY:` |
| [08-git-standards.md](standards/08-git-standards.md) | Conventional Commits (`feat\|fix\|test\|docs\|...`), Git Flow, nunca `git add .`, PRs a `develop` |
| [09-feature-workflow.md](standards/09-feature-workflow.md) | El ciclo completo: Spec aprobada → ADR si aplica → Domain → Tests (Red) → Código (Green) → Docs → Review |
| [10-ai-agent-rules.md](standards/10-ai-agent-rules.md) | Lo que un agente puede decidir solo y lo que requiere confirmación humana |

---

### `testing/` — Estrategia de testing

Define la pirámide de tests del proyecto: qué se testea con tests unitarios, qué con
tests de integración, qué con tests de contrato. Incluye umbrales de cobertura mínima
por capa y las reglas sobre cuándo se pueden usar mocks vs. bases de datos reales.

El enfoque es **SDD (Spec-Driven Development)**: los test cases se derivan directamente
de los criterios de aceptación de la spec antes de escribir el código de producción.

→ [Estándares de testing](testing/01-testing-standards.md)

---

### `specifications/` — Specs de features

Una spec documenta el comportamiento esperado de una feature **antes** de implementarla.
Contiene: contexto, criterios de aceptación numerados (AC-XX), casos borde, contrato
de la API (endpoints, request/response, códigos de error) y casos de test derivados (TC-XX).

**Ninguna feature se implementa sin spec aprobada.** El estado de cada spec
(`DRAFT` → `APPROVED` → `IMPLEMENTED`) está en el encabezado del archivo.

→ Ver archivos `*.spec.md` en esta carpeta

---

### `modules/` — Documentación técnica por módulo

Documentación de implementación por dominio funcional: auth, RBAC, FHIR, agenda,
historial clínico, obra social, frontend, base de datos, motor IA, notificaciones.
Se crea o actualiza al finalizar cada módulo.

---

## Flujo de trabajo con esta documentación

```
Nueva feature
    │
    ├─→ ¿Hay una decisión técnica no obvia?
    │       └─→ Sí → crear ADR antes de codear
    │
    ├─→ Escribir spec en specifications/ → esperar aprobación
    │
    ├─→ Leer el estándar de la capa afectada (standards/)
    │
    ├─→ Implementar siguiendo el workflow en 09-feature-workflow.md
    │
    └─→ Actualizar spec a IMPLEMENTED · completar JavaDoc · actualizar módulo si aplica
```
