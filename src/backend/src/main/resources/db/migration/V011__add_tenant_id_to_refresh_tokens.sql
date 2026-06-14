-- V011 — refresh_tokens.tenant_id: la sesión de refresh es por tenant (T-003, spec §6)
-- WHY: /refresh debe saber para qué tenant emitir el nuevo accessToken sin depender del body.

ALTER TABLE refresh_tokens
    ADD COLUMN tenant_id UUID NOT NULL
        REFERENCES tenants(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_tenant_id ON refresh_tokens(tenant_id);
