-- V012 — Corrección: columnas de auditoría faltantes según standards §2
--
-- Problemas detectados en análisis DER (2026-06-15):
--
-- 1. appointment_noshow_scores: falta created_by (tabla tiene solo created_at sin FK a users)
-- 2. coverage_weekly_usage: faltan created_at, created_by, updated_by
--    (solo tenía updated_at sin trigger ni columna created_at)
-- 3. notification_log: faltan updated_at (con trigger), updated_by, created_by
-- 4. fhir_search_params: falta FK constraint explícita a tenants en tenant_id
--    (la columna existe pero no tiene constraint declarado)

-- ── 1. appointment_noshow_scores ─────────────────────────────────────────────

ALTER TABLE appointment_noshow_scores
    ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id);

-- ── 2. coverage_weekly_usage ─────────────────────────────────────────────────

ALTER TABLE coverage_weekly_usage
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS created_by  UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by  UUID REFERENCES users(id);

-- El trigger set_updated_at ya existe (V001); aplicarlo a esta tabla.
CREATE TRIGGER trg_coverage_weekly_updated_at
    BEFORE UPDATE ON coverage_weekly_usage
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── 3. notification_log ───────────────────────────────────────────────────────

ALTER TABLE notification_log
    ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS created_by  UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS updated_by  UUID REFERENCES users(id);

CREATE TRIGGER trg_notification_log_updated_at
    BEFORE UPDATE ON notification_log
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── 4. fhir_search_params: agregar FK explícita a tenants ────────────────────

ALTER TABLE fhir_search_params
    ADD CONSTRAINT fk_fhir_search_params_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);
