# Módulo: Historial Clínico (MVP)

> Versión MVP del seminario (mayo–diciembre 2026).
> CIE-10 fuzzy, MedicationRequest, FamilyMemberHistory y resumen LLM
> están en roadmap post-MVP.

## Scope MVP

| Recurso FHIR | En MVP | Nota |
|---|---|---|
| Encounter | Sí | Apertura, registro SOAP, cierre |
| Observation | Sí | Signos vitales, antropometría, mediciones |
| Condition | No (roadmap) | Diagnóstico con CIE-10 fuzzy |
| MedicationRequest | No (roadmap) | Recetas electrónicas |
| FamilyMemberHistory | No (roadmap) | Historial familiar |

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/fhir/resources/encounter.py` | CRUD Encounter (consulta) |
| `backend/app/fhir/resources/observation.py` | CRUD Observation (signos / mediciones) |
| `frontend/components/fhir/SOAPNote.tsx` | Formulario SOAP simple |
| `frontend/components/fhir/HistoriaTimeline.tsx` | Timeline de Encounters del paciente |

## Estructura SOAP en FHIR (Encounter)

```
Encounter:
  status: "planned" | "arrived" | "in-progress" | "finished"
  class: {code: "AMB"} (ambulatorio / presencial)
  subject: Reference(Patient)
  participant[]: Reference(Practitioner)  ← médico
  appointment[]: Reference(Appointment)   ← turno origen

  reasonCode[0].text  → S: Motivo de consulta (Subjetivo)
  note[0].text        → O: Examen físico (Objetivo)
  note[1].text        → A: Análisis (texto libre — sin CIE-10 en MVP)
  note[2].text        → P: Plan de tratamiento (Plan)

  ← Observation[] vinculadas: Observation.encounter = Reference(Encounter)
     Para signos vitales y mediciones.
```

## Máquina de estados del Encounter

```
planned → arrived → in-progress → finished
                        ↓
                    cancelled
```

- Al cerrar Encounter, el Appointment asociado pasa a `fulfilled`.
- Si el médico abandona sin cerrar, queda `in-progress` y se notifica
  en la próxima sesión del médico.

## Patrones Clave (MVP)

- Un Encounter por consulta; múltiples Observations por Encounter.
- Encounter siempre vinculado a un Appointment (restricción ClinicaSaaS).
- Validación de rangos clínicos en Observations → advertencia, no
  bloqueo.

## Roadmap post-MVP

- **CIE-10 fuzzy**: ~17.000 códigos en BD + búsqueda fuse.js
  client-side para diagnóstico estructurado.
- **MedicationRequest**: prescripción electrónica con detección de
  interacciones.
- **FamilyMemberHistory**: alertas por condiciones genéticas
  relevantes.
- **Resumen LLM**: resumen automático al abrir ficha mediante modelo
  de lenguaje sobre los recursos FHIR del paciente.
- **Sugerencia CIE-10 automática**: a partir del motivo de consulta.

## NO HACER

- No guardar SOAP como texto libre sin estructura FHIR (usar los
  campos correctos del Encounter).
- No crear Encounter sin vincularlo a un Appointment existente.
- No agregar CIE-10, MedicationRequest, FamilyMemberHistory ni resumen
  LLM al MVP — están en roadmap explícito.

## Dependencias

→ `.claude/context/modules/03-fhir.md` (patrón base FHIR)
→ `.claude/context/modules/04-agenda.md` (Appointment → Encounter)
→ `docs/plan-de-trabajo.md` (semanas 9–10 — implementación MVP)
→ `.claude/tasks/use-cases.md` (CU-02)
