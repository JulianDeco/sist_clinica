# Módulo: Notificaciones Inteligentes

> Stack: Java 21 + Spring Boot 3 (migrado 2026-06-08).
> MVP del seminario: Telegram Bot API + Email.
> Post-piloto: WhatsApp Business API + SMS.
> ADR-013 documenta la decisión de abstracción de canales.

## Scope de canales

| Canal | Fase | Notas |
|---|---|---|
| Telegram Bot API | **MVP** | Gratuito, sin aprobación, biblioteca `telegrambots` Maven |
| Email SMTP | **MVP** | SendGrid / Mailgun / SMTP propio |
| WhatsApp Business API | Post-piloto | Requiere aprobación Meta + costo/conversación |
| SMS | Post-piloto | Twilio / InfoBip — fallback para sin smartphone |
| Push notification | Roadmap | Web push / mobile |

## Arquitectura (Clean Architecture + Port/Adapter)

```
application/
  notifications/
    NotificationPort.java          ← interfaz — la lógica de CU-03 llama aquí
    NotificationRequest.java       ← record: destinatario, mensaje, canal, appointmentId
    NotificationResult.java        ← record: éxito, messageId, timestamp, canal usado

infrastructure/
  notifications/
    TelegramNotificationAdapter.java   ← implementa NotificationPort — MVP
    EmailNotificationAdapter.java      ← implementa NotificationPort — MVP
    WhatsAppNotificationAdapter.java   ← implementa NotificationPort — post-piloto
    SmsNotificationAdapter.java        ← implementa NotificationPort — post-piloto
    NotificationDispatcher.java        ← selecciona adaptador por canal configurado
    NotificationLogRepository.java     ← persiste notification_log
```

**Regla crítica**: la lógica de CU-03 (qué canal usar, cuándo, con qué mensaje)
vive en el Application Service. Los Adapters solo saben enviar. Nunca al revés.

## Política de notificación diferenciada por riesgo

| Riesgo | Canales | Timing |
|---|---|---|
| Alto (>70) | Telegram + email | 48h antes + 24h antes |
| Medio (30–70) | Telegram + email | 24h antes |
| Bajo (<30) | Email | 24h antes |

Configurable por tenant: umbrales, canales habilitados y timing
en tabla `tenant_notification_config`.

## Job programado

Spring `@Scheduled` — frecuencia configurable (default: cada hora).

Flujo por ejecución:
1. Seleccionar Appointments con fecha en ventana [now+X, now+Y]
2. Excluir los ya notificados en esa ventana (dedup Redis TTL 48h)
3. Obtener score desde `IntelligencePort` (cache Redis 30min)
4. Aplicar política de canal según score
5. Enviar via `NotificationDispatcher`
6. Persistir en `notification_log`

## Manejo de respuestas bidireccionales

Telegram soporta botones de respuesta inline (`InlineKeyboardMarkup`).
El paciente responde con un botón; el bot recibe el callback via webhook.

| Respuesta | Acción |
|---|---|
| "Confirmar asistencia" | `Appointment.status = CONFIRMED` |
| "Necesito reprogramar" | Notificar staff; marcar para reprogramación |
| "No voy a poder" | `Appointment.status = CANCELLED`; liberar Slot |

El webhook de Telegram llama a `POST /api/v1/notifications/telegram/webhook`.

## Fallback y reintentos

- Fallo de canal primario → backoff exponencial (3 intentos: 1min, 5min, 15min)
- Después de 3 intentos → intentar canal secundario (email si falló Telegram)
- Si todos fallan → `notification_log.status = FAILED_ALL_CHANNELS`
  → notificar staff via dashboard (no por canal externo)

## Plantilla de mensaje (Telegram — riesgo alto)

```
Hola {{paciente.nombre}}, te recordamos tu turno con
{{profesional.nombre}} el {{turno.fecha}} a las {{turno.hora}} en
{{clinica.nombre}}.

[✅ Confirmar asistencia]  [🔄 Reprogramar]  [❌ No puedo ir]

Si no podés asistir, avisanos con 24h de anticipación para que
otro paciente pueda usar tu horario. ¡Gracias!
```

Las plantillas se configuran por tenant en `notification_templates`.
El texto del mensaje NO varía según el score — solo cambian canales y timing.

## Deduplicación

Clave Redis: `clinica:{tenantId}:notif:{appointmentId}:{ventana}:{canal}`
TTL: 48 horas. Impide enviar el mismo recordatorio dos veces por error.

## Registro y métricas mínimas

Tabla `notification_log`:
- `appointment_id`, `tenant_id`, `channel`, `status`, `message_id_provider`
- `score_at_send` (score usado al momento del envío — no actualizar después)
- `created_at`

**NO guardar** el contenido del mensaje en logs (contiene PII del paciente).
Solo guardar `template_id` + variables usadas en forma anonimizada.

Métricas exportadas via `GET /api/v1/notifications/stats`:
- Tasa de entrega por canal
- Tasa de confirmación por canal
- Tasa de cancelación recibida

## NO HACER

- No mandar más de una notificación por (appointment, ventana, canal)
- No mandar a Appointments en estado `CANCELLED` o `FULFILLED`
- No exponer el score numérico al paciente
- No hardcodear lógica de canal dentro de los Adapters
- No llamar al Adapter directamente desde el Controller

## Dependencias

→ `docs/adr/ADR-013-notification-channels.md` — decisión de arquitectura
→ `.claude/context/modules/04-agenda.md` — Appointment, estados
→ `.claude/context/modules/09-intelligence.md` — score por turno
→ `.claude/tasks/use-cases.md` — CU-03
→ `docs/standards/01-backend-standards.md` — patrones Port/Adapter
