# Robustez CU-01

CU-01: Reservar turno con validación integral.

```mermaid
sequenceDiagram
    %% Trazabilidad:
    %% Paso 1 → Secretario -> UI «selecciona paciente, profesional y franja»
    %% Paso 2 → UI -> VDISP ; VDISP -> AGE ; VDISP -> FRA          (CU-S03)
    %% Paso 3 → VDISP -> VCOB ; VCOB -> COB                        (CU-S04)
    %% Paso 4 → VCOB -> RIESGO ; RIESGO -> PAC ; RIESGO -> PRED    (CU-S05)
    %% Paso 5 → Secretario -> UI «confirma la reserva»
    %% Paso 6 → UI -> REG ; REG -> TUR ; REG -> FRA ; REG -> COB ; REG -> PRED
    actor Secretario as Secretario/a
    participant UI as Formulario de reserva [boundary]
    participant VDISP as Validar disponibilidad de slot [control]
    participant SUG as Sugerir slots alternativos [control]
    participant VCOB as Validar cobertura y tope semanal [control]
    participant RIESGO as Calcular riesgo de no-show [control]
    participant REG as Registrar reserva [control]
    participant AGE as Agenda [entity]
    participant FRA as FranjaHoraria [entity]
    participant COB as Cobertura [entity]
    participant PAC as Paciente [entity]
    participant PRED as PrediccionDeRiesgo [entity]
    participant TUR as Turno [entity]

    Note over Secretario,UI: Paso 1
    Secretario->>UI: selecciona paciente, profesional y franja horaria

    Note over UI,FRA: Paso 2 — CU-S03 Validar disponibilidad de slot
    UI->>VDISP: verificar disponibilidad
    VDISP->>AGE: consulta la agenda del profesional
    VDISP->>FRA: verifica franja libre y ausencia de conflictos

    alt A1 — slot ya ocupado (CU-S08)
        VDISP->>SUG: solicitar alternativas
        SUG->>FRA: busca franjas cercanas disponibles
        SUG->>UI: muestra slots alternativos
        Secretario->>UI: elige un slot alternativo
        Note over VDISP: El flujo retoma en el paso 2 con el nuevo slot.
    end

    Note over VCOB,COB: Paso 3 — CU-S04 Validar cobertura y tope semanal
    VDISP->>VCOB: slot disponible
    VCOB->>COB: verifica vigencia y descuenta del tope semanal

    alt A2 — cobertura vencida o tope superado
        VCOB->>UI: bloquea imputación y advierte a la secretaria
        UI-->>Secretario: informa que la cobertura no es válida
        Secretario->>UI: decide si continuar el turno sin imputación a cobertura
        Note over Secretario: Decisión 2026-06-10: el turno puede crearse sin cobertura (multiplicidad 0..1). La decisión es del actor, no del sistema.
    end

    Note over RIESGO,PRED: Paso 4 — CU-S05 Calcular riesgo de no-show
    VCOB->>RIESGO: cobertura procesada (válida o sin imputación)
    RIESGO->>PAC: obtiene historial de asistencia
    RIESGO->>PRED: genera score (0–100) y factores influyentes
    RIESGO->>UI: muestra score y factores en lenguaje natural

    Note over RIESGO: A3 — sin historial: score con factores parciales, marcado «baja confianza».\nA4 — falla del motor: el turno se crea sin score, registrado para reintento.\nA5 — score > 70% y overbooking habilitado: se dispara CU-04.

    Note over Secretario,UI: Paso 5
    Secretario->>UI: confirma la reserva

    Note over UI,PRED: Paso 6 — transacción atómica
    UI->>REG: registrar la reserva
    REG->>TUR: crea el turno con el score de riesgo asociado
    REG->>FRA: marca la franja como ocupada
    REG->>COB: consolida el descuento del tope semanal
    REG->>PRED: asocia la predicción al turno creado

    Note over REG,PRED: Las modificaciones de Turno, FranjaHoraria y Cobertura se aplican atómicamente.
```
