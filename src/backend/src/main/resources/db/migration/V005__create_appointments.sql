-- V005 — appointments, appointment_noshow_scores, coverage_weekly_usage

-- ── appointments ──────────────────────────────────────────────────────────────
-- Proyección relacional del FHIR Appointment para JOINs eficientes.
-- Status FHIR R4: proposed|pending|booked|arrived|fulfilled|cancelled|noshow|
--                 entered-in-error|checked-in|waitlist
CREATE TABLE IF NOT EXISTS appointments (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id            UUID         NOT NULL,
    fhir_resource_id     UUID         NOT NULL,
    fhir_appointment_id  VARCHAR(64)  NOT NULL,
    patient_fhir_id      VARCHAR(64)  NOT NULL,
    practitioner_fhir_id VARCHAR(64)  NOT NULL,
    slot_fhir_id         VARCHAR(64)  NOT NULL,
    appointment_date     DATE         NOT NULL,
    start_time           TIMESTAMPTZ  NOT NULL,
    end_time             TIMESTAMPTZ  NOT NULL,
    status               VARCHAR(30)  NOT NULL,
    is_overbooked        BOOLEAN      NOT NULL DEFAULT FALSE,
    cancellation_reason  VARCHAR(255),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by           UUID,
    updated_by           UUID,
    deleted_at           TIMESTAMPTZ,
    deleted_by           UUID,

    CONSTRAINT pk_appointments                 PRIMARY KEY (id),
    CONSTRAINT uq_appointments_fhir_id         UNIQUE (tenant_id, fhir_appointment_id),
    CONSTRAINT ck_appointments_times           CHECK (end_time > start_time),
    CONSTRAINT fk_appointments_tenant          FOREIGN KEY (tenant_id)        REFERENCES tenants(id),
    CONSTRAINT fk_appointments_fhir_resource   FOREIGN KEY (fhir_resource_id) REFERENCES fhir_resources(id),
    CONSTRAINT fk_appointments_created_by      FOREIGN KEY (created_by)       REFERENCES users(id),
    CONSTRAINT fk_appointments_updated_by      FOREIGN KEY (updated_by)       REFERENCES users(id),
    CONSTRAINT fk_appointments_deleted_by      FOREIGN KEY (deleted_by)       REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_appointments_tenant_id     ON appointments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_appointments_tenant_date   ON appointments(tenant_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_patient       ON appointments(tenant_id, patient_fhir_id);
CREATE INDEX IF NOT EXISTS idx_appointments_practitioner  ON appointments(tenant_id, practitioner_fhir_id);
CREATE INDEX IF NOT EXISTS idx_appointments_status        ON appointments(tenant_id, status) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── appointment_noshow_scores ─────────────────────────────────────────────────
-- Historial de scores del motor heurístico. Score vigente = MAX(calculated_at).
-- DELETE físico permitido (registro histórico).
CREATE TABLE IF NOT EXISTS appointment_noshow_scores (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL,
    appointment_id UUID        NOT NULL,
    calculated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    score          SMALLINT    NOT NULL,
    confidence     VARCHAR(10) NOT NULL,
    model_version  VARCHAR(20) NOT NULL,
    factors        JSONB       NOT NULL,

    CONSTRAINT pk_noshow_scores           PRIMARY KEY (id),
    CONSTRAINT ck_noshow_score_range      CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT ck_noshow_confidence       CHECK (confidence IN ('high','medium','low')),
    CONSTRAINT fk_noshow_scores_tenant    FOREIGN KEY (tenant_id)      REFERENCES tenants(id),
    CONSTRAINT fk_noshow_scores_appt      FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- Índice para recuperar score vigente (más reciente) en una lectura — FR-16
CREATE INDEX IF NOT EXISTS idx_noshow_scores_appt_date
    ON appointment_noshow_scores(appointment_id, calculated_at DESC);
CREATE INDEX IF NOT EXISTS idx_noshow_scores_tenant_id
    ON appointment_noshow_scores(tenant_id);

-- ── coverage_weekly_usage ─────────────────────────────────────────────────────
-- Tope semanal de cobertura. Semana ISO: lunes 00:00 → domingo 23:59.
-- DELETE físico permitido (registro histórico).
CREATE TABLE IF NOT EXISTS coverage_weekly_usage (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL,
    patient_fhir_id  VARCHAR(64) NOT NULL,
    coverage_fhir_id VARCHAR(64) NOT NULL,
    week_start_date  DATE        NOT NULL,
    usage_count      SMALLINT    NOT NULL DEFAULT 0,
    weekly_limit     SMALLINT    NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_coverage_weekly          PRIMARY KEY (id),
    CONSTRAINT uq_coverage_weekly_key      UNIQUE (tenant_id, patient_fhir_id, coverage_fhir_id, week_start_date),
    CONSTRAINT ck_coverage_usage_count     CHECK (usage_count >= 0),
    CONSTRAINT ck_coverage_weekly_limit    CHECK (weekly_limit > 0),
    CONSTRAINT fk_coverage_weekly_tenant   FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX IF NOT EXISTS idx_coverage_weekly_tenant_id ON coverage_weekly_usage(tenant_id);

CREATE TRIGGER trg_coverage_weekly_updated_at
    BEFORE UPDATE ON coverage_weekly_usage
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
