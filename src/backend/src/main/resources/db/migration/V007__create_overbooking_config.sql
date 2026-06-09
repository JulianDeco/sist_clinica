-- V007 — overbooking_config
-- Configuración del motor CU-04 por profesional. Soft delete sí (OQ-09).
-- UNIQUE parcial: solo un registro activo por profesional por tenant.

CREATE TABLE IF NOT EXISTS overbooking_config (
    id                        UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id                 UUID        NOT NULL,
    practitioner_fhir_id      VARCHAR(64) NOT NULL,
    enabled                   BOOLEAN     NOT NULL DEFAULT FALSE,
    risk_threshold            SMALLINT    NOT NULL DEFAULT 70,
    max_weekly_overbookings   SMALLINT    NOT NULL DEFAULT 5,
    excluded_appointment_types JSONB      NOT NULL DEFAULT '[]',
    created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by                UUID,
    updated_by                UUID,
    deleted_at                TIMESTAMPTZ,
    deleted_by                UUID,

    CONSTRAINT pk_overbooking_config             PRIMARY KEY (id),
    CONSTRAINT ck_overbooking_risk_threshold     CHECK (risk_threshold BETWEEN 0 AND 100),
    CONSTRAINT fk_overbooking_config_tenant      FOREIGN KEY (tenant_id)  REFERENCES tenants(id),
    CONSTRAINT fk_overbooking_config_created_by  FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_overbooking_config_updated_by  FOREIGN KEY (updated_by) REFERENCES users(id),
    CONSTRAINT fk_overbooking_config_deleted_by  FOREIGN KEY (deleted_by) REFERENCES users(id)
);

-- Unique parcial: un solo registro activo por (tenant, practitioner)
CREATE UNIQUE INDEX IF NOT EXISTS uq_overbooking_config_active
    ON overbooking_config(tenant_id, practitioner_fhir_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_overbooking_config_tenant_id ON overbooking_config(tenant_id);

CREATE TRIGGER trg_overbooking_config_updated_at
    BEFORE UPDATE ON overbooking_config
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
