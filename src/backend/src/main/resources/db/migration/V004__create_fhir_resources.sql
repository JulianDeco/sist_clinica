-- V004 — FHIR storage: fhir_resources + fhir_search_params
-- Patrón validado por HAPI FHIR y Smile CDR.
-- Un único tabla para todos los resource types del MVP.

CREATE TABLE IF NOT EXISTS fhir_resources (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID        NOT NULL,
    fhir_id       VARCHAR(64) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    version_id    INTEGER     NOT NULL DEFAULT 1,
    resource_data JSONB       NOT NULL,
    resource_hash CHAR(64)    NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by    UUID,
    updated_by    UUID,
    deleted_at    TIMESTAMPTZ,
    deleted_by    UUID,

    CONSTRAINT pk_fhir_resources         PRIMARY KEY (id),
    CONSTRAINT uq_fhir_resources_logical UNIQUE (tenant_id, resource_type, fhir_id),
    CONSTRAINT fk_fhir_resources_tenant  FOREIGN KEY (tenant_id)  REFERENCES tenants(id),
    CONSTRAINT fk_fhir_resources_created FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_fhir_resources_updated FOREIGN KEY (updated_by) REFERENCES users(id),
    CONSTRAINT fk_fhir_resources_deleted FOREIGN KEY (deleted_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_fhir_resources_tenant_id   ON fhir_resources(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fhir_resources_type_tenant ON fhir_resources(resource_type, tenant_id);
CREATE INDEX IF NOT EXISTS idx_fhir_resources_fhir_id     ON fhir_resources(fhir_id);
-- GIN index para búsquedas JSONB — obligatorio por NFR-03
CREATE INDEX IF NOT EXISTS idx_fhir_resources_data_gin    ON fhir_resources USING gin(resource_data);

CREATE TRIGGER trg_fhir_resources_updated_at
    BEFORE UPDATE ON fhir_resources
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── fhir_search_params ────────────────────────────────────────────────────────
-- Parámetros de búsqueda extraídos del JSONB para evitar full-scans.
CREATE TABLE IF NOT EXISTS fhir_search_params (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID         NOT NULL,
    fhir_resource_id UUID         NOT NULL,
    param_name       VARCHAR(100) NOT NULL,
    param_value      TEXT         NOT NULL,
    param_type       VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_fhir_search_params        PRIMARY KEY (id),
    CONSTRAINT fk_fhir_search_params_res    FOREIGN KEY (fhir_resource_id) REFERENCES fhir_resources(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fhir_search_params_tenant       ON fhir_search_params(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fhir_search_params_resource_id  ON fhir_search_params(fhir_resource_id);
CREATE INDEX IF NOT EXISTS idx_fhir_search_params_name_value   ON fhir_search_params(tenant_id, param_name, param_value);
