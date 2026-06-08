-- V009 — audit_log
-- Registro inmutable de acciones de usuario. Solo INSERT — nunca UPDATE/DELETE.
-- FK a tenants cumple ADR-003. Purga permitida para registros > 2 años.

CREATE TABLE IF NOT EXISTS audit_log (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    user_email    VARCHAR(255) NOT NULL,
    action        VARCHAR(50)  NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id   VARCHAR(64),
    old_values    JSONB,
    new_values    JSONB,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    request_id    VARCHAR(36),
    occurred_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_audit_log          PRIMARY KEY (id),
    CONSTRAINT ck_audit_log_action   CHECK (action IN (
        'CREATE','UPDATE','DELETE',
        'LOGIN','LOGOUT','FAILED_LOGIN',
        'PERMISSION_DENIED','TOKEN_REVOKED'
    )),
    CONSTRAINT fk_audit_log_tenant   FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Índices para consultas paginadas de auditoría — NFR-09, NFR-10
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant_date
    ON audit_log(tenant_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant_user_date
    ON audit_log(tenant_id, user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_resource
    ON audit_log(tenant_id, resource_type, resource_id);
