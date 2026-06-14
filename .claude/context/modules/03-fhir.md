# Módulo: FHIR R4 Core

## Archivos

| Archivo | Rol |
|---|---|
| `backend/app/fhir/router.py` | Prefijo `/fhir/R4/`, incluye todos los resource routers |
| `backend/app/fhir/capability.py` | `GET /fhir/R4/metadata` → CapabilityStatement |
| `backend/app/fhir/search.py` | Convierte FHIR search params → queries SQL/JSONB |
| `backend/app/fhir/resources/*.py` | Un archivo por recurso FHIR |

## Recursos implementados (MVP)

| Recurso FHIR | Archivo | Tabla BD |
|---|---|---|
| Patient | `resources/patient.py` | `fhir_resources` (JSONB) |
| Practitioner + PractitionerRole | `resources/practitioner.py` | `fhir_resources` |
| Schedule + Slot | `resources/schedule.py` | `fhir_resources` |
| Appointment | `resources/appointment.py` | `fhir_resources` + `sobreturno_log` |
| Encounter | `resources/encounter.py` | `fhir_resources` |
| Condition (CIE-10) | `resources/condition.py` | `fhir_resources` |
| MedicationRequest | `resources/medication_request.py` | `fhir_resources` |
| Coverage | `resources/coverage.py` | `fhir_resources` |
| FamilyMemberHistory | `resources/family_history.py` | `fhir_resources` |
| ChargeItem | `resources/charge_item.py` | `fhir_resources` |

## API — Patrón estándar por recurso

| Operación | Método + Path |
|---|---|
| Crear | `POST /fhir/R4/{Resource}` |
| Leer | `GET /fhir/R4/{Resource}/{id}` |
| Actualizar | `PUT /fhir/R4/{Resource}/{id}` |
| Eliminar | `DELETE /fhir/R4/{Resource}/{id}` |
| Buscar | `GET /fhir/R4/{Resource}?param=value` |

## Validación del recurso FHIR

```python
# Ejemplo conceptual del stack legacy (Python) — en Spring Boot la validación
# del recurso FHIR R4 ocurre en el Controller antes de persistir el JSONB
from fhir.resources.patient import Patient

async def crear_paciente(patient: Patient, ...):
    data = patient.model_dump()
    # guardar en fhir_resources.data (JSONB)
```

## Patrones Clave

- `fhir_resources.data` guarda el recurso FHIR completo como JSONB
- `fhir_search_params` indexa campos para búsquedas eficientes (evita JSONB full-scan)
- Respuestas siempre con `Content-Type: application/fhir+json`
- Bundle para búsquedas: `{"resourceType": "Bundle", "type": "searchset", "entry": [...]}`
- CIE-10: ~17.000 códigos precargados en BD, búsqueda fuzzy en frontend con `fuse.js`

## NO HACER

- No guardar recursos FHIR en tablas separadas por tipo (usar tabla genérica JSONB)
- No inventar campos no FHIR dentro del recurso — usar `extension[]` si es necesario
- No responder sin `resourceType` en el JSON
- No validar FHIR manualmente — dejar que `fhir.resources` haga el trabajo

## Dependencias

→ `.claude/context/modules/04-agenda.md` (Appointment, Schedule, Slot)
→ `.claude/context/modules/05-historial.md` (Encounter, Condition, MedicationRequest)
→ `.claude/context/modules/08-database.md` (fhir_resources, fhir_search_params)
