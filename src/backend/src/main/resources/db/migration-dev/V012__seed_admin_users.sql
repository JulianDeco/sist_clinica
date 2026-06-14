-- V012 — Seed DEV ONLY: tenant demo + usuario ADMIN para pruebas de login (T-003, FR-10)
-- SOLO se ejecuta en perfil `dev` (classpath:db/migration-dev, application.yml).
-- NUNCA incluir en la secuencia de migraciones de prod.
-- Contraseña: Admin1234! (BCrypt strength 12) — solo para desarrollo local.

INSERT INTO tenants (id, name, slug, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'Clínica Demo', 'clinica-demo', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO users (id, email, password_hash, full_name, active)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    'admin@clinica-demo.com',
    '$2a$12$KIqsVVFHRoqRi0SVZjM1gO7Y0YvGN6hXVBCh4sJDXQjS3CiZ5S/jO',
    'Administrador Demo',
    TRUE
) ON CONFLICT DO NOTHING;

-- Rol ADMIN (id insertado en V002)
INSERT INTO user_tenants (user_id, tenant_id, role_id, active)
SELECT
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000001',
    id,
    TRUE
FROM roles WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;
