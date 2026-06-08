-- V003 — Users, user_roles, refresh_tokens
-- users es tenant-scoped. refresh_tokens es auditoría de sesiones (no soft delete).

CREATE TABLE IF NOT EXISTS users (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    UUID,
    updated_by    UUID,

    CONSTRAINT pk_users              PRIMARY KEY (id),
    CONSTRAINT uq_users_email        UNIQUE (email),
    CONSTRAINT fk_users_tenant       FOREIGN KEY (tenant_id)  REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_created_by   FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_users_updated_by   FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── user_roles ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_roles       PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ── refresh_tokens ────────────────────────────────────────────────────────────
-- DELETE físico permitido para tokens expirados (job de purga).
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    jti               VARCHAR(36)  NOT NULL,
    token_hash        VARCHAR(255) NOT NULL,
    issued_at         TIMESTAMPTZ  NOT NULL,
    expires_at        TIMESTAMPTZ  NOT NULL,
    revoked_at        TIMESTAMPTZ,
    revocation_reason VARCHAR(100),
    device_id         VARCHAR(255),
    user_agent        VARCHAR(500),
    ip_address        VARCHAR(45),
    last_used_at      TIMESTAMPTZ,

    CONSTRAINT pk_refresh_tokens      PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_jti  UNIQUE (jti),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
