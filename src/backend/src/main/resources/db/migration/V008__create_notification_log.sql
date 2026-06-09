-- V008 — notification_log
-- Registro de envíos para CU-03. Una fila por intento.
-- DELETE físico permitido (registro histórico — OQ-09).

CREATE TABLE IF NOT EXISTS notification_log (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID         NOT NULL,
    appointment_id   UUID         NOT NULL,
    channel          VARCHAR(20)  NOT NULL,
    message_type     VARCHAR(20)  NOT NULL,
    recipient        VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    sent_at          TIMESTAMPTZ,
    response_at      TIMESTAMPTZ,
    response_payload JSONB,
    retry_count      SMALLINT     NOT NULL DEFAULT 0,
    error_message    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_notification_log              PRIMARY KEY (id),
    CONSTRAINT ck_notification_log_channel      CHECK (channel      IN ('TELEGRAM','EMAIL','SMS')),
    CONSTRAINT ck_notification_log_message_type CHECK (message_type IN ('REMINDER','CONFIRMATION','CANCELLATION','FOLLOW_UP')),
    CONSTRAINT ck_notification_log_status       CHECK (status       IN ('PENDING','SENT','FAILED','CONFIRMED','CANCELLED')),
    CONSTRAINT fk_notification_log_tenant       FOREIGN KEY (tenant_id)      REFERENCES tenants(id),
    CONSTRAINT fk_notification_log_appointment  FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX IF NOT EXISTS idx_notification_log_tenant_id    ON notification_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notification_log_appointment  ON notification_log(appointment_id);
CREATE INDEX IF NOT EXISTS idx_notification_log_status       ON notification_log(tenant_id, status);
