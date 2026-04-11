-- Configuraciones de seguridad iniciales para PostgreSQL
-- Este archivo se ejecuta UNA SOLA VEZ al crear el contenedor

-- Revocar acceso público por defecto
REVOKE ALL ON DATABASE clinica_db FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- El usuario de la app ya tiene la DB creada por POSTGRES_USER/DB env vars.
-- Darle permisos mínimos necesarios (sin superuser).
GRANT CONNECT ON DATABASE clinica_db TO clinica_user;
GRANT USAGE, CREATE ON SCHEMA public TO clinica_user;

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- búsquedas fuzzy en JSONB
