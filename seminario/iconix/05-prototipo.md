# Prototipo — T-020 entregable 4

> La consigna pide un prototipo "lo más básico posible (papel si es posible)".
> ClinicaSaaS ya cuenta con un scaffold Angular 18 navegable (T-002), por lo
> que el prototipo de baja fidelidad se compone de dos partes.

## Parte 1 — Pantallas implementadas (scaffold T-002)

Capturas del frontend real en `src/frontend/` (levantar con `pnpm start` y
capturar en 1280×800):

| Pantalla | Ruta | CU relacionado | Captura |
|---|---|---|---|
| Landing pública | `/` | — (presentación) | `prototipo/01-landing.png` *(pendiente)* |
| Login | `/login` | Iniciar sesión | `prototipo/02-login.png` *(pendiente)* |
| Selección de clínica | `/select-tenant` | Seleccionar tenant (ADR-014) | `prototipo/03-select-tenant.png` *(pendiente)* |
| Layout principal + switcher | `/app` | — (shell de navegación) | `prototipo/04-layout.png` *(pendiente)* |

## Parte 2 — Pantallas aún no implementadas (boceto en papel)

Para los CU core cuyas pantallas no existen todavía, el prototipo es un boceto
en papel fotografiado (válido según la consigna). Bocetos necesarios, derivados
de los boundaries de los diagramas de robustez (03–06):

| Boceto | Boundary (robustez) | CU |
|---|---|---|
| Formulario de reserva con score de riesgo y sugerencia de sobreturno | "Formulario de reserva" | CU-01 / CU-04 |
| Agenda con sobreturnos diferenciados visualmente | "Pantalla de agenda" | CU-04 |
| Pantalla de consulta SOAP (notas + observaciones) | "Pantalla de consulta" | CU-02 |
| Mensaje de recordatorio al paciente (Telegram/email) con confirmar/cancelar | "Mensaje de recordatorio" | CU-03 |

Guardar las fotos de los bocetos en `prototipo/bocetos/`.

## Justificación

El prototipo combina evidencia real (el flujo de autenticación two-step ya
navegable) con bocetos de papel para las pantallas de los 4 CU core, que es
exactamente el nivel de fidelidad que la consigna admite. Los bocetos se
trazan desde los objetos boundary de los diagramas de robustez, manteniendo
la trazabilidad ICONIX prototipo ↔ robustez ↔ casos de uso.
