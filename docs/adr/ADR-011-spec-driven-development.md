# ADR-011: Spec-Driven Development como Workflow Obligatorio

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, estándares de testing, workflow de features

---

## Contexto

Kuris es un proyecto académico con requisitos estrictos de documentación,
trazabilidad y justificación de decisiones de implementación (Seminario de Trabajo
Final UAI 2026). El desarrollo asistido por IA introduce el riesgo de generar código
que no coincida con los requisitos previstos o que introduzca comportamientos no documentados.

Además, con un solo desarrollador trabajando en backend, frontend e infraestructura,
existe el riesgo de implementar features desde la memoria o suposición en lugar de
requisitos explícitos.

## Decisión

Adoptar **Spec-Driven Development (SDD)** como primer paso obligatorio para cada
implementación de feature:

1. Escribir un archivo de spec (`{Feature}.spec.md`) antes de cualquier código de
   implementación
2. El spec debe ser aprobado explícitamente antes de comenzar a codificar
3. El spec define: reglas de negocio (BR-XX), casos de prueba (TC-XX), entradas, salidas
4. Los tests se escriben a partir de los ítems TC-XX (TDD desde el spec)
5. El spec se confirma junto con la implementación en el mismo PR

Los archivos de spec residen en `docs/modules/{module}/specs/`.

## Consecuencias

**Positivo:**
- Cada feature tiene un contrato escrito — defendible en la tesis
- La generación de código por IA está limitada por el spec (no puede agregar
  comportamientos no documentados)
- Los casos de prueba se derivan de los requisitos, no del código (dirección TDD correcta)
- Cadena de trazabilidad: caso de uso → spec → test → implementación
- Preguntas abiertas se detectan antes de la implementación (no se descubren a mitad
  del código)

**Negativo / compromisos:**
- Agrega ~30 minutos por feature para la redacción del spec
- Requiere disciplina: tentación de saltarlo para features "pequeñas"

**Riesgos:**
- El spec se vuelve obsoleto si no se actualiza cuando la implementación se desvía:
  mitigado requiriendo actualización del estado del spec a IMPLEMENTED antes del merge

## Notas

Esto está inspirado en procesos RFC-first usados en grandes empresas tecnológicas
(Design Docs de Google, PRFAQs de Amazon) adaptados a la escala de un proyecto
académico de un solo desarrollador.
