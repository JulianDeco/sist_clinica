# Módulo: Obra Social / Tope Semanal

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/modules/obra_social/models.py` | `ObraSocial`, `PractitionerObraSocialConfig` |
| `backend/app/modules/obra_social/router.py` | CRUD obra_social + config tope por médico |
| `backend/app/modules/obra_social/service.py` | Cálculo tope + cache Redis + background task |
| `frontend/app/(dashboard)/obra-social/page.tsx` | Panel: config tope + semáforo % semanal |

## API

| Endpoint | Método | Descripción |
|---|---|---|
| `/obra-social` | GET/POST | Listar y crear obras sociales |
| `/obra-social/{id}/config` | GET/PUT | Config tope por médico (requiere role medico) |
| `/obra-social/{id}/estado-semana` | GET | % utilizado semana actual por médico |

## Lógica de tope — ALERTA, NUNCA BLOQUEO

```
El tope es una RECOMENDACIÓN. El médico siempre puede crear el turno.

Al crear Appointment con obra social que superó el tope:
  1. Backend calcula porcentaje actual
  2. Si porcentaje >= tope_configurado:
     → Respuesta 200 OK (el turno SE CREA igual)
     → Body incluye: {"warning": "tope_alcanzado", "porcentaje": 18.3,
                      "tope": 15.0, "semana_sugerida": "2024-W12"}
  3. Frontend muestra modal de confirmación:
     "Llegaste al 18.3% de IAPOS (tu límite es 15%).
      ¿Querés agendar igual o preferís pasarlo a la semana que viene?"
     [Agendar igual]  [Mover a próxima semana]
  4. El médico decide — no hay bloqueo

Semáforo visual en dashboard:
  < 70%  → verde  (sin alerta)
  70-90% → amarillo (⚠ cerca del límite)
  ≥ tope → rojo   (🔴 superó el límite — puede seguir agendando)
```

## Cálculo del porcentaje

```python
# Background task cada hora + al registrar nuevo ChargeItem
total_semana = SUM(ChargeItem.amount) WHERE practitioner=X AND semana=actual AND tenant=T
obra_semana  = SUM WHERE practitioner=X AND obra_social=Y AND semana=actual AND tenant=T
porcentaje   = (obra_semana / total_semana) * 100

# Cache Redis: TTL 1 hora — incluye tenant_id en la key
key = f"tope:{tenant_id}:{practitioner_id}:{obra_social_id}:{iso_week}"
# Invalidar al registrar nuevo ChargeItem de ese médico en ese tenant
```

## Tablas BD

```sql
-- GLOBAL (sin tenant_id): las obras sociales son entidades nacionales
-- (OSDE, IOMA, PAMI, IAPOS, etc.) — existen independientemente de cada clínica
obra_social(id, nombre, codigo, descripcion)

-- POR TENANT: la configuración del tope es específica de cada médico en cada clínica
practitioner_obra_social_config(
  tenant_id,        -- aislamiento por clínica
  practitioner_id,  -- Practitioner FHIR id
  obra_social_id,
  tope_porcentaje,  -- ej: 15.0
  UNIQUE(tenant_id, practitioner_id, obra_social_id)
)
```

## Patrones Clave

- Tope = recomendación personal del médico, no regla de negocio dura
- El warning viaja en el body de la respuesta 200, no como error 4xx
- `Coverage` FHIR vincula paciente ↔ obra social (fuente de verdad del paciente)
- El médico puede ignorar la recomendación → el sistema registra el turno normalmente
- En Fase 2: el historial de decisiones (ignoró/aceptó la recomendación) alimenta reportes

## NO HACER

- No retornar 409 ni bloquear la creación del turno por tope de obra social
- No calcular el porcentaje en cada request (usar cache Redis)
- No hardcodear lista de obras sociales (configurable por super_admin)

## Dependencias

→ `.claude/context/modules/04-agenda.md` (flujo al crear turno)
→ `.claude/context/modules/03-fhir.md` (Coverage, ChargeItem — implementado en T-012b)
