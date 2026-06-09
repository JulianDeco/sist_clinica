-- V002 — Roles, permisos, role_permissions + seed
-- Tablas globales (sin tenant_id) — RBAC compartido por todos los tenants.

CREATE TABLE IF NOT EXISTS roles (
    id      UUID        NOT NULL DEFAULT gen_random_uuid(),
    name    VARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles      PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS permissions (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    description VARCHAR(255),

    CONSTRAINT pk_permissions      PRIMARY KEY (id),
    CONSTRAINT uq_permissions_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT pk_role_permissions        PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role   FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_perm   FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ── Seed: roles ──────────────────────────────────────────────────────────────
INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('DOCTOR'),
    ('SECRETARY')
ON CONFLICT (name) DO NOTHING;

-- ── Seed: permissions ────────────────────────────────────────────────────────
INSERT INTO permissions (name, module, description) VALUES
    -- APPOINTMENTS (núcleo)
    ('APPOINTMENT_READ',   'APPOINTMENTS', 'Ver turnos'),
    ('APPOINTMENT_CREATE', 'APPOINTMENTS', 'Crear turno'),
    ('APPOINTMENT_UPDATE', 'APPOINTMENTS', 'Modificar turno'),
    ('APPOINTMENT_CANCEL', 'APPOINTMENTS', 'Cancelar turno'),
    -- ENCOUNTERS (núcleo)
    ('ENCOUNTER_READ',     'ENCOUNTERS',   'Ver consultas'),
    ('ENCOUNTER_CREATE',   'ENCOUNTERS',   'Abrir consulta'),
    ('ENCOUNTER_UPDATE',   'ENCOUNTERS',   'Modificar consulta'),
    ('PATIENT_READ',       'ENCOUNTERS',   'Ver pacientes'),
    ('PATIENT_CREATE',     'ENCOUNTERS',   'Registrar paciente'),
    -- FHIR_COVERAGE
    ('COVERAGE_READ',      'FHIR_COVERAGE',    'Ver coberturas'),
    ('COVERAGE_UPDATE',    'FHIR_COVERAGE',    'Modificar cobertura'),
    -- OVERBOOKING
    ('OVERBOOKING_CONFIG', 'OVERBOOKING',      'Configurar overbooking'),
    -- NOSHOW_PREDICTION
    ('NOSHOW_SCORE_READ',  'NOSHOW_PREDICTION','Ver scores de ausentismo'),
    -- NOTIFICATIONS
    ('NOTIFICATION_READ',  'NOTIFICATIONS',    'Ver log de notificaciones'),
    -- SYSTEM
    ('USER_MANAGE',        'SYSTEM', 'Gestionar usuarios del tenant'),
    ('TENANT_CONFIG',      'SYSTEM', 'Configurar tenant'),
    ('AUDIT_LOG_READ',     'SYSTEM', 'Ver log de auditoría')
ON CONFLICT (name) DO NOTHING;

-- ── Seed: role_permissions ───────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE
    (r.name = 'ADMIN'     AND p.name IN (
        'APPOINTMENT_READ','APPOINTMENT_CREATE','APPOINTMENT_UPDATE','APPOINTMENT_CANCEL',
        'ENCOUNTER_READ','ENCOUNTER_CREATE','ENCOUNTER_UPDATE',
        'PATIENT_READ','PATIENT_CREATE',
        'COVERAGE_READ','COVERAGE_UPDATE',
        'OVERBOOKING_CONFIG','NOSHOW_SCORE_READ','NOTIFICATION_READ',
        'USER_MANAGE','TENANT_CONFIG','AUDIT_LOG_READ'))
OR  (r.name = 'DOCTOR'    AND p.name IN (
        'APPOINTMENT_READ','APPOINTMENT_CREATE','APPOINTMENT_UPDATE','APPOINTMENT_CANCEL',
        'ENCOUNTER_READ','ENCOUNTER_CREATE','ENCOUNTER_UPDATE',
        'PATIENT_READ',
        'COVERAGE_READ',
        'OVERBOOKING_CONFIG','NOSHOW_SCORE_READ'))
OR  (r.name = 'SECRETARY' AND p.name IN (
        'APPOINTMENT_READ','APPOINTMENT_CREATE','APPOINTMENT_UPDATE','APPOINTMENT_CANCEL',
        'PATIENT_READ','PATIENT_CREATE',
        'COVERAGE_READ'))
ON CONFLICT DO NOTHING;
