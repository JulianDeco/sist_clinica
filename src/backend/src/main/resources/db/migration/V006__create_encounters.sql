-- V006 — encounters
-- Proyección relacional del FHIR Encounter. Relación 1:N con appointments (OQ-10).
-- Estado FHIR R4: planned|arrived|triaged|in-progress|onleave|finished|cancelled

CREATE TABLE IF NOT EXISTS encounters (
    id                   UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id            UUID        NOT NULL,
    fhir_resource_id     UUID        NOT NULL,
    fhir_encounter_id    VARCHAR(64) NOT NULL,
    appointment_id       UUID        NOT NULL,
    -- Proyección denormalizada para queries de agenda sin JOIN a appointments (FR-17)
    patient_fhir_id      VARCHAR(64) NOT NULL,
    practitioner_fhir_id VARCHAR(64) NOT NULL,
    status               VARCHAR(20) NOT NULL,
    class_code           VARCHAR(20) NOT NULL DEFAULT 'AMB',
    started_at           TIMESTAMPTZ,
    finished_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by           UUID,
    updated_by           UUID,
    deleted_at           TIMESTAMPTZ,
    deleted_by           UUID,

    CONSTRAINT pk_encounters                PRIMARY KEY (id),
    CONSTRAINT uq_encounters_fhir_id        UNIQUE (tenant_id, fhir_encounter_id),
    CONSTRAINT fk_encounters_tenant         FOREIGN KEY (tenant_id)        REFERENCES tenants(id),
    CONSTRAINT fk_encounters_fhir_resource  FOREIGN KEY (fhir_resource_id) REFERENCES fhir_resources(id),
    CONSTRAINT fk_encounters_appointment    FOREIGN KEY (appointment_id)   REFERENCES appointments(id),
    CONSTRAINT fk_encounters_created_by     FOREIGN KEY (created_by)       REFERENCES users(id),
    CONSTRAINT fk_encounters_updated_by     FOREIGN KEY (updated_by)       REFERENCES users(id),
    CONSTRAINT fk_encounters_deleted_by     FOREIGN KEY (deleted_by)       REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_encounters_tenant_id     ON encounters(tenant_id);
CREATE INDEX IF NOT EXISTS idx_encounters_appointment   ON encounters(appointment_id);
CREATE INDEX IF NOT EXISTS idx_encounters_patient       ON encounters(tenant_id, patient_fhir_id);
CREATE INDEX IF NOT EXISTS idx_encounters_status        ON encounters(tenant_id, status) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_encounters_updated_at
    BEFORE UPDATE ON encounters
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
