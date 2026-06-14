# Módulo: Agenda / Turnos / Sobreturnos

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/fhir/resources/appointment.py` | CRUD Appointment + lógica sobreturno |
| `backend/app/fhir/resources/schedule.py` | CRUD Schedule + Slot (disponibilidad) |
| `backend/app/modules/obra_social/service.py` | Validación tope obra social al crear turno |
| `backend/app/modules/agenda/models.py` | Tabla `sobreturno_log` |
| `frontend/app/(dashboard)/agenda/page.tsx` | Vista semanal con badge sobreturno |
| `frontend/components/fhir/AppointmentSlot.tsx` | Slot visual con indicador sobreturno |

## API

| Endpoint | Método | Descripción |
|---|---|---|
| `/fhir/R4/Appointment` | POST | Crear turno (normal o sobreturno) |
| `/fhir/R4/Appointment/{id}` | GET/PUT | Leer/actualizar turno |
| `/fhir/R4/Appointment?date=&practitioner=` | GET | Buscar turnos por fecha y médico |
| `/fhir/R4/Schedule?actor=` | GET | Disponibilidad del médico |
| `/fhir/R4/Slot?schedule=&status=free` | GET | Slots libres |

## Sobreturno — Reglas de negocio

```
Appointment.serviceType[0].coding[0].code = "sobreturno" → es sobreturno
Appointment.priority = 0 (normal) | 1 (urgente)

Al crear sobreturno:
  - No consume Slot estándar (no vincula a un Slot existente)
  - Insertar en sobreturno_log: {appointment_id, suggested_by="manual", approved_by=user_id}
  - Alerta médico si tiene >2 sobreturnos en el día

Sobreturno IA (Fase 2):
  - suggested_by = "ai", ai_confidence = 0.0-1.0
  - Requiere aprobación explícita del médico/recepcionista
  - decision = "approved" | "rejected" → retroalimenta el modelo
```

## Validación tope obra social al crear turno

```
Al POST /fhir/R4/Appointment:
  1. Obtener obra_social del paciente (Coverage)
  2. Consultar Redis: "tope:{tenant_id}:{practitioner_id}:{obra_social_id}:semana_actual"
  3. Si cache miss → calcular desde BD → guardar Redis TTL 1h
  4. Si porcentaje >= tope → responder 200 OK con warning en body:
       {"warning": "tope_alcanzado", "porcentaje": 18.3, "tope": 15.0,
        "semana_sugerida": "2024-W12"}
     El turno SE CREA igual — el tope es una recomendación, no un bloqueo
     (ver regla completa en 06-obra-social.md)
```

## Patrones Clave

- Estados Appointment: `pending` → `booked` → `fulfilled` | `cancelled` | `noshow`
- `Appointment.minutesDuration` = duración estimada (comparar con Encounter.period para métricas)
- Slot libre → status `free`; al bookear → status `busy`
- Buscar appointments por semana: `?date=ge2024-01-01&date=le2024-01-07`

## NO HACER

- No retornar 409 por tope de obra social — es 200 + warning, nunca bloqueo
- No marcar Slot como busy si es sobreturno (no consume slot)
- No permitir sobreturno IA sin aprobación humana
- No omitir `tenant_id` en `sobreturno_log` al insertar

## Dependencias

→ `.claude/context/modules/03-fhir.md` (patrón base FHIR)
→ `.claude/context/modules/06-obra-social.md` (validación tope)
→ `.claude/context/modules/09-intelligence.md` (motor heurístico no-show y overbooking)
→ `.claude/tasks/use-cases.md` (CU-01 reservar turno, CU-04 overbooking)
