# Roadmap — mayo a diciembre 2026

Proyecto de tesis del Seminario de Trabajo Final — UAI Rosario.
Carga real disponible: **15–20 h/semana × 32 semanas ≈ 480–640 h**.

## Fases

| Fase | Período | Semanas | Carga est. | Entregable clave |
|---|---|---|---|---|
| **1 — Cimientos y agenda** | 12/05 → 12/07 | 9 | ~125 h | Scaffold backend/frontend, auth JWT, FHIR base, agenda interactiva |
| **2 — Historia clínica y obra social** | 13/07 → 16/08 | 5 | ~70 h | Encounter SOAP, validación tope semanal obra social |
| **3 — Inteligencia operacional** | 17/08 → 27/09 | 6 | ~100 h | Predicción ausentismo, recordatorios por riesgo, overbooking |
| **4 — Validación con piloto** | 28/09 → 25/10 | 4 | ~65 h | Piloto real o simulado, feedback, ajustes |
| **5 — Redacción y defensa** | 26/10 → 31/12 | 8 | ~130 h | Tesis redactada, defensa, holgura general |

**Total estimado**: ~490 h · **Disponible**: 480–640 h.

## Hitos de control

| Fecha | Hito |
|---|---|
| 19/05/2026 | Entrega 1 UAI — plan de negocios inicial |
| 16/06/2026 | Entrega 2 UAI — plan completo + ICONIX |
| 08/07/2026 | Semana de absorción Fase 1 |
| 15/08/2026 | Cierre ICONIX parcial |
| 28/09/2026 | Cierre ICONIX completo + fin Fase 3 |
| 30/11/2026 | MVP entregado |
| Diciembre 2026 | Defensa |

## Criterio de recorte

Si se acumula más de 1 semana de atraso, se recorta en este orden:

1. Resumen LLM + sugerencia CIE-10
2. Overbooking inteligente
3. Recordatorios diferenciados por riesgo
4. Predicción heurística de ausentismo
5. Obra social tope semanal
6. Historia clínica frontend
7. Agenda interactiva

!!! danger "Nunca se recortan"
    Multitenant y FHIR core son inamovibles — son el núcleo académico y técnico del proyecto.

## Dentro del MVP

- FHIR R4 multitenant (Patient, Practitioner, Appointment, Encounter, Observation, Coverage)
- Agenda interactiva, ficha de paciente, historia clínica SOAP básica
- Obra social con validación de tope semanal
- Predicción heurística explicable de ausentismo
- Recordatorios inteligentes diferenciados por riesgo
- Overbooking inteligente
- Resumen automático de consulta con LLM (Claude API)
- Sugerencia de código CIE-10 desde texto libre
- 1 piloto real (o plan B simulado y documentado)

## Post-MVP (roadmap comercial)

- Cobertura óptima automatizada por franja horaria
- ML entrenado por tenant (cuando acumule volumen suficiente)
- Importador masivo desde Excel
- Alta automática de tenant
- Marketplace B2C de turnos para pacientes finales
- Integraciones específicas con obras sociales vía FHIR
