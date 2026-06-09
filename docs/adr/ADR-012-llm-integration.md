# ADR-012: IA Generativa mediante Claude API (Anthropic) para Asistencia Clínica

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: T-017, T-018, UC-02

---

## Context

ClinicaSaaS necesita incorporar capacidades de IA real en el MVP del seminario.
Dos tareas administrativas en el flujo clínico diario son candidatas naturales:
(1) redactar el resumen de una consulta a partir de datos FHIR estructurados,
(2) sugerir el código CIE-10 a partir de texto libre del motivo de consulta.
Ambas son repetitivas, consumen tiempo del médico y tienen salida verificable.

## Problem

¿Cómo integrar IA generativa de manera controlada, económicamente viable para
una clínica piloto, y sin introducir dependencias de infraestructura pesada?

## Options Considered

| Option | Summary |
|---|---|
| Claude API (Anthropic) | API REST, pago por uso, modelos Haiku/Sonnet, latencia < 2s, sin infraestructura propia |
| OpenAI GPT-4o-mini | API REST similar, pago por uso, ecosistema más conocido |
| Modelo local (Ollama + Llama 3) | Sin costo por llamada, requiere GPU o CPU potente, latencia alta en VPS 4GB |
| Python microservicio + scikit-learn | Solo para clasificación, no generación; no sirve para resumen |

## Decision

Usar **Claude API (Anthropic)**, modelo **claude-haiku-4-5** para CIE-10 (bajo
costo, respuesta estructurada) y **claude-sonnet-4-6** para resumen clínico
(mayor calidad de redacción médica).

Integración desde Spring Boot mediante `RestClient` (Spring 6) o `WebClient`.
Sin frameworks de IA (LangChain4j evaluado pero descartado para MVP — overhead
innecesario para dos casos de uso bien definidos).

El módulo vive en `infrastructure/ai/` con dos clases:
- `ClinicalSummaryClient.java` — resumen de Encounter
- `Cie10SuggestionClient.java` — sugerencia de código

## Consequences

**Positive:**
- Sin infraestructura adicional — una llamada HTTP desde Spring Boot
- Costo estimado por clínica piloto (50 consultas/día): ~USD 3–8/mes
- Latencia aceptable: < 2s para Haiku, < 4s para Sonnet
- El médico valida el output — la IA asiste, no decide
- Académicamente valioso: integración real con LLM documentada en tesis

**Negative:**
- Dependencia de servicio externo — si la API no está disponible, la funcionalidad degrada gracefully (el médico completa manualmente)
- Costo variable: a mayor volumen de consultas, mayor costo de API
- Datos clínicos salen del VPS hacia Anthropic — mitigado: no se envían datos identificatorios del paciente (nombre, DNI), solo datos clínicos anonimizados del Encounter

## Tradeoffs

- Calidad vs. costo: Haiku es suficiente para CIE-10 (tarea de clasificación);
  Sonnet es preferible para resumen (redacción más coherente). Si el costo
  escala, se puede bajar a Haiku para ambos.
- Privacidad: se debe definir un prompt que no incluya nombre ni DNI del paciente.
  Solo se envía: fecha, motivo de consulta, notas SOAP, observaciones clínicas.
- Fallback: si la API falla, el médico completa manualmente. No es un flujo
  bloqueante — la consulta puede cerrarse sin resumen automático.

## Notes

- Modelo IDs actuales: `claude-haiku-4-5-20251001`, `claude-sonnet-4-6`
- La interfaz del servicio (`AiAssistancePort`) se diseña para que sea
  intercambiable con otro proveedor sin cambiar el dominio (ADR-010 Clean Architecture)
- Registrar en `docs/adr/README.md` y actualizar tabla de índice
