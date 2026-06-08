# ADR-013: Abstracción de Canales de Notificación — Telegram MVP, WhatsApp Post-Piloto

**Status**: ACCEPTED
**Date**: 2026-06-08
**Author**: Julián Deco
**Relates to**: T-009, UC-03

---

## Context

CU-03 requiere enviar notificaciones a pacientes por distintos canales según
el score de riesgo de ausentismo. Los canales candidatos son Telegram, WhatsApp,
email y SMS. Cada uno tiene diferente costo, complejidad de integración y
requisitos de aprobación.

WhatsApp Business API requiere aprobación de Meta (proceso de días a semanas),
tiene costo por conversación (~USD 0.05 en Argentina), y requiere un número de
teléfono dedicado verificado. No es viable para el piloto inicial.

Telegram Bot API es gratuita, sin aprobación, sin límite de mensajes, y tiene
una biblioteca Java madura (TelegramBots).

## Problem

¿Cómo implementar notificaciones comenzando con Telegram para el piloto, sin
bloquear la incorporación de WhatsApp y SMS en producción, y sin que la lógica
de negocio de CU-03 tenga que cambiar al agregar nuevos canales?

## Options Considered

| Option | Summary |
|---|---|
| Implementar solo WhatsApp desde el inicio | Requiere aprobación Meta + costo; no viable para MVP |
| Implementar Telegram hardcodeado en el servicio | Rápido pero imposible de extender sin refactor |
| Port/Adapter con NotificationPort (Clean Architecture) | La lógica de negocio llama al puerto; los adaptadores son intercambiables |

## Decision

Diseñar el módulo de notificaciones con una **interfaz de puerto**
(`NotificationPort`) que la capa de aplicación llama sin conocer el canal
concreto. Los adaptadores se registran como beans Spring y se seleccionan
según el canal configurado por tenant o por score de riesgo.

```
application/
  notifications/
    NotificationPort.java          ← interfaz del dominio
    NotificationRequest.java       ← record: destinatario, mensaje, canal, prioridad
    NotificationResult.java        ← record: éxito/fallo, messageId, timestamp

infrastructure/
  notifications/
    TelegramNotificationAdapter.java   ← MVP (gratis, sin aprobación)
    EmailNotificationAdapter.java      ← MVP (SMTP)
    WhatsAppNotificationAdapter.java   ← Post-piloto (Meta Business API)
    SmsNotificationAdapter.java        ← Post-piloto (Twilio/InfoBip)
    NotificationDispatcher.java        ← selecciona adaptador por canal
```

**Canales en MVP (seminario)**:
- Telegram Bot API — alto y medio riesgo
- Email SMTP (Gmail/SendGrid) — todos los riesgos

**Canales post-piloto**:
- WhatsApp Business API — reemplaza o complementa Telegram en producción
- SMS — fallback para pacientes sin smartphone

La selección de canal por score queda en el Application Service (CU-03),
no en los adaptadores.

## Consequences

**Positive:**
- Piloto arranca el día 1 sin aprobaciones externas ni costo variable
- Agregar WhatsApp en producción = implementar un nuevo Adapter + configurar credenciales; cero cambios en lógica de negocio
- Los pacientes del piloto que no usen Telegram igual reciben email
- Testeable en aislamiento: cada Adapter tiene su propio unit test con mock

**Negative:**
- Telegram no es el canal dominante en el segmento médico adulto argentino — WhatsApp tiene mayor penetración; el piloto puede tener menor tasa de respuesta por este motivo
- Requiere que el paciente tenga cuenta Telegram y haya iniciado conversación con el bot (limitación del modelo de bots de Telegram)

## Tradeoffs

La limitación de Telegram (el paciente debe iniciar la conversación con el bot
primero) es el trade-off central del MVP. Se mitiga documentando el proceso de
onboarding del paciente como parte del onboarding de la clínica piloto.

Si la tasa de respuesta por Telegram es significativamente menor que la
esperada, se acelera la integración de WhatsApp antes de lo planificado.

## Notes

- Biblioteca Java para Telegram: `org.telegram:telegrambots` (Maven)
- La interfaz `NotificationPort` se diseña para soportar mensajes con botones
  de respuesta (confirmar/cancelar turno), que Telegram soporta nativamente
  mediante `InlineKeyboardMarkup`
- La respuesta del paciente (confirmar/cancelar) actualiza el estado del
  Appointment vía webhook — diseñar desde el inicio para este flujo bidireccional
