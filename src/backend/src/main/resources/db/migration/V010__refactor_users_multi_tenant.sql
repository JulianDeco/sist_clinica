-- V010 — Refactor users: separar identidad de membresía (ADR-014)
-- Reemplaza el modelo user.tenant_id + user_roles por:
--   users (identidad global, sin tenant_id)
--   user_tenants (membresía: user × tenant × role)

-- ── 1. Eliminar constraints que dependen de tenant_id en users ────────────────

ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_tenant;
ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_users_email;
DROP INDEX  IF EXISTS idx_users_tenant_id;

-- ── 2. Eliminar columna tenant_id de users ────────────────────────────────────

ALTER TABLE users DROP COLUMN IF EXISTS tenant_id;

-- ── 3. Restaurar unicidad global de email ─────────────────────────────────────

ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

-- ── 4. Eliminar tabla user_roles (reemplazada por user_tenants) ───────────────

DROP TABLE IF EXISTS user_roles;

-- ── 5. Crear tabla user_tenants ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_tenants (
    user_id   UUID        NOT NULL,
    tenant_id UUID        NOT NULL,
    role_id   UUID        NOT NULL,
    active    BOOLEAN     NOT NULL DEFAULT TRUE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_tenants       PRIMARY KEY (user_id, tenant_id),
    CONSTRAINT fk_ut_user            FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_ut_tenant          FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ut_role            FOREIGN KEY (role_id)   REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_user_tenants_user_id   ON user_tenants(user_id);
CREATE INDEX IF NOT EXISTS idx_user_tenants_tenant_id ON user_tenants(tenant_id);

-- ── 6. refresh_tokens: quitar FK a users.tenant_id (ya no existe) ─────────────
-- La tabla refresh_tokens solo referencia users(id) — sin cambios necesarios.

-- ── Comentario de diseño ──────────────────────────────────────────────────────
-- Un usuario puede pertenecer a N tenants con roles distintos.
-- Un usuario tiene exactamente un rol por tenant (MVP; extender post-MVP si se necesitan
-- roles múltiples por tenant).
-- El flujo de auth emite primero un identity token (5 min, purpose=tenant-select)
-- y luego un session token con tenant_id+role una vez seleccionado el tenant.
