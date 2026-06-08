-- V001 — Tenants + trigger set_updated_at
-- Tabla global (sin tenant_id). Fuente de verdad de clínicas registradas.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Trigger reutilizable para mantener updated_at en toda tabla
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS tenants (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    slug            VARCHAR(63) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    plan            VARCHAR(50) NOT NULL DEFAULT 'free',
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    enabled_modules JSONB       NOT NULL DEFAULT '["APPOINTMENTS","ENCOUNTERS"]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_tenants       PRIMARY KEY (id),
    CONSTRAINT uq_tenants_slug  UNIQUE (slug)
);

CREATE TRIGGER trg_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
