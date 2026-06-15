# Workflow de Desarrollo de Features

> Aplica a: toda feature, corrección de bug, refactor y mejora.
> Metodología: SDD → TDD → Clean Architecture → DDD → ADR.
> **Nunca saltear pasos. Nunca adivinar reglas de negocio. Detenerse y preguntar
> cuando haya ambigüedad.**

---

## Ciclo TDD — Reglas Explícitas

Toda unidad de implementación (entidad de dominio, caso de uso, endpoint de controlador,
componente Angular) sigue este ciclo sin excepción:

```
RED      → Escribir un test fallando que exprese el requisito.
            Ejecutar mvn test / ng test → confirmar que FALLA.
            Si pasa de inmediato, el test está mal o es redundante.

GREEN    → Escribir el código de producción mínimo para que el test pase.
            Sin lógica extra, sin limpieza "mientras estoy aquí".
            Ejecutar mvn test / ng test → confirmar que PASA.

REFACTOR → Limpiar sin cambiar el comportamiento.
            Renombrar, extraer, simplificar. Volver a ejecutar los tests → siguen en GREEN.
            Solo entonces confirmar el commit.
```

**Bloqueantes estrictos de TDD:**

| Situación | Acción requerida |
|---|---|
| Escribir código de producción sin test fallando | DETENER — escribir el test primero |
| El test pasa en la primera ejecución antes de cualquier implementación | ELIMINAR — no prueba nada |
| Saltear la fase Red ("sé que fallará") | NO aceptable — ejecutarlo, ver que falla |
| Confirmar código Green antes del Refactor | Solo permitido si el refactor es un commit separado |
| Mockear la base de datos en tests de integración | PROHIBIDO — usar Testcontainers |

**Alcance de TDD por capa (backend):**

| Capa | Tipo de test | Herramientas |
|---|---|---|
| Entidad de dominio / value object | Unitario | JUnit 5 + AssertJ |
| Servicio de aplicación / caso de uso | Unitario | JUnit 5 + Mockito |
| Controlador (round-trip HTTP) | Integración | `@SpringBootTest` + MockMvc + Testcontainers |
| Repositorio (SQL + aislamiento de tenant) | Integración | `@DataJpaTest` + Testcontainers |

**Alcance de TDD por capa (frontend):**

| Capa | Tipo de test | Herramientas |
|---|---|---|
| Store (estado con Signals) | Unitario | Jasmine + Karma |
| Servicio de cliente de API | Unitario | `HttpClientTestingModule` |
| Guard / interceptor | Unitario | TestBed |
| Componente contenedor | Componente | Angular Testing Library |

---

## Orden Obligatorio de Desarrollo

```
1. Especificación        ← sin código antes de que esto esté APROBADO
2. Análisis de impacto ADR  ← decisiones arquitectónicas antes del diseño de dominio
3. Diseño de dominio    ← DDD: entidades, value objects, agregados, eventos
4. Diseño de tests      ← TC-XX derivados de AC-XX en el spec; stubs de tests creados
5. TDD — Fase Red       ← escribir tests fallando; mvn test / ng test confirma FALLO
6. TDD — Fase Green     ← código de producción mínimo para pasar tests; confirmar PASA
7. TDD — Refactor       ← limpiar; tests siguen en GREEN; commit
8. Documentación        ← spec IMPLEMENTED, JavaDoc, doc del módulo, ADR
9. Revisión             ← PR contra develop, checklist, aprobación
```

---

## Definition of Ready

Una tarea no puede comenzar implementación hasta que TODOS estos puntos sean verdaderos:

- [ ] Especificación escrita y **aprobada explícitamente**
- [ ] Criterios de aceptación definidos (AC-XX en el spec)
- [ ] Casos límite identificados
- [ ] Dependencias conocidas (otros módulos, recursos FHIR, cambios en BD)
- [ ] Impacto ADR evaluado (¿esta decisión requiere un nuevo ADR?)

---

## Definition of Done

Una tarea está completa solo cuando TODOS estos puntos son verdaderos:

- [ ] Estado de la especificación actualizado a `IMPLEMENTED`
- [ ] Todos los tests implementados y pasando (`mvn verify` / `ng test`)
- [ ] Umbral de cobertura mantenido (80% backend / 75% frontend)
- [ ] Toda la documentación actualizada (JavaDoc, doc del módulo, ADR si corresponde)
- [ ] Arquitectura respetada — sin violaciones de capas
- [ ] Sin code smells críticos
- [ ] PR revisado y aprobado
- [ ] `tasks.json` → `estado: hecho` + `fecha_fin`

---

## Paso 1 — Especificación (SDD)

**Objetivo**: Producir el contrato escrito antes de escribir cualquier código.

**Ubicación**: `docs/specifications/{NombreFeature}.spec.md`

Todo spec debe incluir:

| Sección | Contenido |
|---|---|
| Objetivo de negocio | Por qué existe esta feature |
| Requisitos funcionales | Qué debe hacer el sistema (FR-XX) |
| Requisitos no funcionales | Rendimiento, seguridad, restricciones (NFR-XX) |
| Criterios de aceptación | Condiciones verificables para "hecho" (AC-XX) |
| Casos límite | Todos los escenarios fuera del camino feliz |
| Restricciones | Límites técnicos o de negocio |
| Dependencias | Otros módulos, recursos FHIR, servicios externos |
| Riesgos | Qué podría salir mal |
| Preguntas abiertas | Decisiones no resueltas — **resolver antes de implementar** |

**Bloqueante**: ninguna implementación comienza antes de que el estado del spec sea `APPROVED`.
Si los requisitos son ambiguos → detenerse y preguntar. Nunca adivinar reglas de negocio.

Template de spec: `docs/specifications/_template.spec.md`

---

## Paso 2 — Análisis de Impacto ADR

**Objetivo**: Confirmar que la arquitectura existente maneja esta feature, o decidir y
documentar cómo debe evolucionar.

Acciones:
- Leer `docs/architecture/01-high-level-architecture.md`
- Identificar qué capas se tocan (Dominio / Aplicación / Infraestructura / Presentación)
- Buscar patrones existentes en `src/` — reutilizar antes de crear
- Preguntar: ¿requiere esto una nueva decisión arquitectónica?
  - Si **sí** → escribir ADR en `docs/adr/` **antes** del diseño de dominio
  - Si **no** → documentar confirmación en el spec (referenciar ADRs existentes)
- Validar impacto en BD: ¿se necesita migración?
- Validar impacto en caché: ¿claves de Redis a invalidar?

**Regla**: nunca generar código que eluda la arquitectura definida.
Preferir soluciones simples. Preferir composición sobre herencia.
Evitar abstracciones prematuras y sobreingeniería.

---

## Paso 3 — Diseño de Dominio (DDD)

**Objetivo**: Modelar el dominio antes de tocar infraestructura o frameworks.

Acciones:
- Identificar **Entidades** (tienen identidad, estado mutable)
- Identificar **Value Objects** (definidos por valor, inmutables)
- Identificar **Agregados** (límite de consistencia) y su raíz
- Identificar **Eventos de Dominio** (algo que ocurrió)
- Identificar **Servicios de Dominio** (lógica que no pertenece a una sola entidad)
- Mapear máquinas de estados para entidades con ciclo de vida (por ejemplo, estado del turno)

**Regla**: la capa de dominio no debe depender de ningún framework (sin anotaciones de Spring
en los objetos de dominio excepto las de mapeo JPA).
Las reglas de negocio pertenecen únicamente a las capas de Dominio y Aplicación.

Entregable: diagrama de clases (Mermaid) agregado al spec o doc del módulo.

---

## Paso 4 — Diseño de Tests

**Objetivo**: Derivar casos de prueba directamente de los criterios de aceptación del spec.

Acciones:
- Para cada AC-XX en el spec, escribir al menos un TC-XX (caso de prueba)
- Clasificar cada test: unitario (caso de uso / dominio) o integración (controlador / repositorio)
- Para tests de integración: identificar requisitos de Testcontainers
- Definir test de aislamiento de tenant (obligatorio para cada nuevo método de repositorio)
- Documentar la lista TC-XX en el archivo de spec bajo `## Casos de Prueba`

Entregable: lista TC-XX en el spec; stubs de clase de tests creados (métodos `@Test` vacíos
anotados con `@Disabled("TDD: not yet implemented")` para que el build permanezca verde
hasta que el Paso 5 elimine la anotación e implemente la aserción).

---

## Paso 5 — Fase Red de TDD (tests fallando primero)

**Objetivo**: Toda clase de producción está precedida por un test fallando. Sin excepciones.

Orden:
1. Crear clase de test para entidad de dominio — escribir aserciones para transiciones
   de estado e invariantes. La clase de dominio **no existe aún** — el archivo de test no
   compilará. Eso es esperado y correcto.
2. Crear clase de test para caso de uso de aplicación — mockear todos los colaboradores
   con Mockito. La clase de caso de uso **no existe aún**.
3. Crear clase de test de integración para controlador — configurar MockMvc, Testcontainers.
4. Crear test de integración de repositorio — incluir aserción de aislamiento de tenant.
5. Ejecutar `mvn test` (backend) o `ng test` (frontend).
   **Resultado requerido**: errores de compilación o fallos en los tests. Si todo
   pasa, el test está mal — corregirlo antes de continuar.

**Regla**: la fase Red termina solo cuando `mvn test` muestra tests fallando (no
pasando, no advertencias de compilación — fallos o errores reales).

Los nombres de test siguen: `metodo_dadaCondicion_comportamientoEsperado`

---

## Paso 6 — Fase Green de TDD (código mínimo para pasar)

**Objetivo**: Hacer pasar los tests fallando con el menor código necesario.

Orden (backend — de adentro hacia afuera, Clean Architecture):
1. Entidad de dominio / value object → hace que los tests unitarios de dominio compilen y pasen
2. Interfaz de repositorio (en `domain/`) → necesaria por el caso de uso
3. Caso de uso de aplicación → hace que los tests unitarios del caso de uso pasen
4. Implementación JPA de repositorio en infraestructura → hace que los tests de integración
   del repositorio pasen
5. Adaptador Redis si se necesita caché
6. Controlador + DTOs → hace que los tests de integración del controlador pasen
7. Clases de excepción para nuevos casos de error

Ejecutar `mvn test` después de cada clase. Permanecer en la fase Green hasta que todos
los tests pasen.

**Regla**: nunca agregar lógica más allá de lo que el test fallando exige. Si ningún test
lo requiere, no se implementa.

---

## Paso 6b — Fase Refactor de TDD

**Objetivo**: Mejorar la estructura sin cambiar el comportamiento.

Acciones:
- Renombrar para mayor claridad, extraer métodos privados, eliminar duplicación
- Ejecutar `mvn test` después de cada cambio → debe permanecer en GREEN
- Si un refactor rompe un test, revertir — el test es correcto, el refactor está mal

Entregable: código limpio con todos los tests pasando. Confirmar commit aquí.

---

## Paso 7 — Documentación

**Objetivo**: Dejar cada artefacto en un estado listo para revisión de tesis y
sesiones futuras.

Acciones:
- Actualizar estado del spec → `IMPLEMENTED`
- Actualizar `docs/modules/{module}.md` si la máquina de estados o la API cambió
- Escribir/actualizar JavaDoc en todas las nuevas clases y métodos públicos
- Escribir ADR si se tomó una decisión arquitectónica significativa durante la implementación
- Agregar entrada a `docs/adr/decisions-log.md` para decisiones informales
- Actualizar `tasks.json` → `estado: testeando`

---

## Paso 8 — Revisión + Merge

**Objetivo**: Validar contra la Definition of Done antes de mergear.

Acciones:
- Abrir PR contra `develop` usando el template de PR (`docs/standards/08-git-standards.md`)
- Auto-revisión contra el checklist de DoD anterior
- Solicitar revisión a @julian
- Abordar todos los comentarios de revisión
- Merge solo con aprobación
- Actualizar `tasks.json` → `estado: hecho` + `fecha_fin`

---

## Matriz de Trazabilidad

Toda feature debe mantener esta cadena de extremo a extremo:

```
Caso de Uso (use-cases.md)
  └── Tarea (tasks.json  ← fuente única de verdad)
        └── Rama (feature/T-XXX-nombre)
              └── Spec (docs/specifications/{Feature}.spec.md)
                    └── ADR (docs/adr/ADR-NNN-*.md) si corresponde
                          └── Casos de prueba (TC-XX en el spec)
                                └── Tests (nombrados según TC-XX)
                                      └── Implementación (dominio → aplicación → infra → api)
                                            └── PR (enlaza tarea + spec + ADR)
```

Esta cadena permite al comité de tesis, a un revisor o a una sesión futura
seguir cualquier requisito de negocio hasta el código que lo implementa.

---

## Reglas Estrictas

| Regla | Consecuencia de infringir |
|---|---|
| Sin spec → sin código | Bloqueado — escribir el spec primero |
| Requisito ambiguo | Detenerse y preguntar — nunca adivinar |
| Test antes del código de producción | Bloqueado — escribir el test fallando primero |
| Violación de capas | Rechazado en revisión de código |
| tenant_id faltante en consulta | Problema de seguridad — corrección obligatoria antes del merge |
| JavaDoc faltante en API pública | Rechazado en revisión de código |
| `git add .` | Bloqueado — agregar archivos específicos únicamente |
| Modificar migración Flyway ya confirmada | Bloqueado — agregar nueva migración en su lugar |
