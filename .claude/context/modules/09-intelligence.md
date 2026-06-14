# Módulo: Motor de Inteligencia (heurístico no-show + overbooking)

> MVP del seminario (mayo–diciembre 2026).
> ML por tenant queda en roadmap post-MVP — ver `00-stack.md`.

## Alcance MVP

| Capacidad | En MVP | Cómo |
|---|---|---|
| Predicción heurística de no-show | Sí | Reglas explicables sobre historial + contexto del turno |
| Recordatorios diferenciados por riesgo | Sí | Ver `10-notifications.md` |
| Overbooking inteligente | Sí | Detección de slots de alto riesgo + sugerencia |
| ML Random Forest por tenant | No (roadmap) | Cuando cada tenant acumule volumen |
| SHAP / explicabilidad ML | No (roadmap) | Reemplazado por explicaciones heurísticas en MVP |
| Cobertura óptima por franja | No (roadmap) | Requiere demand forecasting |

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/modules/intelligence/noshow.py` | Motor heurístico de score |
| `backend/app/modules/intelligence/factors.py` | Cálculo de factores explicables |
| `backend/app/modules/intelligence/overbooking.py` | Lógica de sugerencia + validación de tope |
| `backend/app/modules/intelligence/service.py` | Interfaz pública consumida por agenda |
| `backend/app/modules/intelligence/router.py` | Endpoints `/turnos/{id}/riesgo-noshow` |

## API

| Endpoint | Método | Descripción |
|---|---|---|
| `/turnos/{appointment_id}/riesgo-noshow` | GET | Score + factores explicables |
| `/turnos/{appointment_id}/overbooking-sugerido` | GET | Si el slot es candidato a overbooking |
| `/profesionales/{id}/overbooking-config` | GET/PUT | Configuración por profesional |

## Algoritmo heurístico de no-show

Factores considerados (cada uno aporta entre 0 y 30 puntos al score
de 0–100):

| Factor | Lógica |
|---|---|
| Historial del paciente | `% de no-shows en últimos 12 meses × 30` |
| Anticipación del turno | <24h → +0, 24-72h → +5, 3-7d → +10, >7d → +15 |
| Día de la semana | Según tasa histórica de ausentismo del tenant ese día |
| Franja horaria | Según tasa histórica de ausentismo del tenant esa franja |
| Es primera consulta | +10 puntos (los primerizos faltan más) |

Score final: suma de factores, clamped a [0, 100].

Rangos:
- 0–30: riesgo bajo
- 31–70: riesgo medio
- 71–100: riesgo alto

## Output explicable

```json
{
  "appointment_id": "abc-123",
  "score": 78,
  "level": "alto",
  "confidence": "media",
  "factors": [
    {
      "code": "patient_history",
      "weight": 25,
      "text": "El paciente faltó a 2 de sus últimos 5 turnos"
    },
    {
      "code": "anticipation",
      "weight": 15,
      "text": "El turno fue agendado con 10 días de anticipación"
    },
    {
      "code": "time_slot",
      "weight": 20,
      "text": "Los turnos de 19:30 tienen 32% de ausentismo en esta clínica"
    }
  ]
}
```

## Overbooking inteligente

Reglas de sugerencia (todas deben cumplirse):

1. Score del turno > umbral del profesional (default 70).
2. Profesional tiene overbooking habilitado.
3. Cantidad de overbookings esta semana < tope configurado (default 5).
4. Tipo de turno no excluido (sin primera consulta, sin cirugía).
5. Slot no tiene ya overbooking aplicado.

Comportamiento:
- El sistema **sugiere**, no aplica automáticamente.
- El usuario confirma; el sistema crea segundo Appointment con
  `Appointment.serviceType[0].coding[0].code = "sobreturno"`.
- Se registra en `sobreturno_log` con `suggested_by = "heuristic"`,
  `confidence = score/100`, `approved_by = user_id`.
- Si se cancela el primer turno, el overbooking pasa a turno normal
  automáticamente.

## Caché y performance

- Score calculado on-demand al consultar; cache Redis TTL 6h por
  appointment_id.
- Estadísticas históricas del tenant (tasas por día / franja)
  cacheadas TTL 24h.
- Latencia objetivo: < 50ms para una consulta de score.

## Patrones Clave

- El motor es **stateless** salvo por consultas SQL agregadas.
- La interfaz pública (input: appointment_id, output: score + factors)
  se diseña desde MVP para que la migración a ML sea transparente.
- Cualquier consumidor del score (agenda, recordatorios, overbooking)
  pasa por `service.py`, nunca llama a `noshow.py` directamente.

## NO HACER

- No agregar dependencias de scikit-learn en MVP — heurística pura.
- No bloquear creación de turno por riesgo alto — el score es
  informativo / sugerencia.
- No exponer factores numéricos sin texto explicable (la
  interpretabilidad es el diferencial frente a una caja negra).
- No cachear el score más de 6h — el historial del paciente cambia.

## Dependencias

→ `.claude/context/modules/04-agenda.md` (Appointment, sobreturno_log)
→ `.claude/context/modules/10-notifications.md` (recordatorios por riesgo)
→ `.claude/tasks/use-cases.md` (CU-01, CU-03, CU-04)
→ `docs/plan-de-trabajo.md` (semanas 15–20 — implementación)
