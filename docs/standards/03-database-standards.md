# Estándares de Base de Datos — PostgreSQL 16

---

## 1. Convenciones de Nomenclatura

| Objeto | Convención | Ejemplo |
|---|---|---|
| Tablas | `snake_case`, plural | `appointments`, `fhir_resources` |
| Columnas | `snake_case` | `tenant_id`, `created_at` |
| Claves primarias | `id` (UUID) | `id UUID PRIMARY KEY` |
| Claves foráneas | `{tabla_referenciada_singular}_id` | `patient_id`, `practitioner_id` |
| Índices | `idx_{tabla}_{columnas}` | `idx_appointments_tenant_date` |
| Restricciones únicas | `uq_{tabla}_{columnas}` | `uq_users_tenant_email` |
| Restricciones de verificación | `ck_{tabla}_{regla}` | `ck_appointments_dates` |
| Secuencias | `{tabla}_{columna}_seq` | (evitar — usar UUIDs como PK) |
| Tipos enum | `snake_case` | `appointment_status` |

---

## 2. Columnas Estándar (toda tabla con alcance de tenant)

```sql
-- Toda tabla que almacene datos de tenant DEBE tener estas columnas
id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
tenant_id   UUID        NOT NULL REFERENCES tenants(id),
created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
created_by  UUID        REFERENCES users(id),
updated_by  UUID        REFERENCES users(id)
```

`updated_at` se mantiene mediante un trigger reutilizable (no depender del código
de la aplicación):

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar a cada tabla
CREATE TRIGGER trg_{table}_updated_at
BEFORE UPDATE ON {table}
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

---

## 3. Política de Borrado Lógico

Todas las entidades de negocio implementan borrado lógico. **Nunca usar DELETE físico**
en registros de negocio.

```sql
-- Columnas de borrado lógico
deleted_at  TIMESTAMPTZ,    -- NULL significa activo; no-nulo significa eliminado
deleted_by  UUID REFERENCES users(id)
```

Reglas:
- Todas las consultas de la aplicación agregan `WHERE deleted_at IS NULL`
  (aplicado por `@Where` de JPA)
- Las consultas de reporte pueden leer registros eliminados explícitamente
- Los borrados físicos solo están permitidos en tablas de auditoría, logs y tokens expirados
- Un job en segundo plano purga registros eliminados hace más de 90 días
  (configurable por tenant)

Anotación de Spring Data JPA en la entidad:
```java
@Entity
@Where(clause = "deleted_at IS NULL")
public class Appointment { ... }
```

---

## 4. Estrategia de Migraciones (Flyway)

- **Flyway** es el único mecanismo para cambios de esquema — nunca `create-all`,
  nunca SQL manual
- Las migraciones residen en `src/main/resources/db/migration/`
- Nomenclatura: `V{version}__{descripcion}.sql` — por ejemplo `V001__create_tenants.sql`
- Los números de versión son enteros secuenciales: V001, V002, V003 ...
- Cada migración debe ser **idempotente** donde sea posible (usar `IF NOT EXISTS`)
- **Nunca modificar una migración ya confirmada** — agregar una nueva en su lugar
- Las migraciones se ejecutan automáticamente al iniciar la aplicación
  (antes de que se sirvan solicitudes)
- Todo PR que agregue una migración debe incluir una nota de rollback en la descripción del PR
  (Flyway Community no soporta rollback automático)

Estructura de archivos de migración:
```
src/main/resources/db/migration/
├── V001__create_tenants.sql
├── V002__create_users_and_roles.sql
├── V003__create_fhir_resources.sql
├── V004__create_appointments.sql
└── V005__create_coverage.sql
```

---

## 5. Estrategia de Índices

Índices predeterminados (creados automáticamente por restricciones):
- Claves primarias
- Restricciones únicas

Índices explícitos requeridos para ClinicaSaaS:

```sql
-- Filtro por tenant — en CADA tabla con tenant_id
CREATE INDEX idx_{table}_tenant ON {table}(tenant_id);

-- Búsquedas de recursos FHIR
CREATE INDEX idx_fhir_resources_type_tenant ON fhir_resources(resource_type, tenant_id);
CREATE INDEX idx_fhir_resources_fhir_id ON fhir_resources(fhir_id);

-- Turnos — patrón de acceso principal
CREATE INDEX idx_appointments_tenant_date ON appointments(tenant_id, appointment_date);
CREATE INDEX idx_appointments_patient ON appointments(tenant_id, patient_id);
CREATE INDEX idx_appointments_practitioner ON appointments(tenant_id, practitioner_id);
CREATE INDEX idx_appointments_status ON appointments(tenant_id, status) WHERE deleted_at IS NULL;

-- Búsqueda JSONB — índice GIN en fhir_resources.resource_data
CREATE INDEX idx_fhir_resources_data_gin ON fhir_resources USING gin(resource_data);
```

Reglas de índices:
- Agregar un índice cuando una consulta filtra por esa columna y se espera que la tabla
  tenga más de 10k filas
- Los índices parciales (`WHERE deleted_at IS NULL`) reducen el tamaño del índice para
  tablas con borrado lógico
- No crear índices compuestos con más de 3 columnas
- Revisar `EXPLAIN ANALYZE` antes de agregar índices especulativos

---

## 6. Reglas de Claves Foráneas

- Todas las columnas FK son `NOT NULL` a menos que la relación sea genuinamente opcional
- `ON DELETE CASCADE` solo para relaciones de propiedad (por ejemplo, `refresh_tokens → users`)
- `ON DELETE RESTRICT` (predeterminado) para relaciones de referencia
- Nunca usar `ON DELETE SET NULL` — preferir manejo explícito de null en la aplicación
- Los índices de FK se crean manualmente (PostgreSQL no indexa automáticamente las columnas FK)

---

## 7. Estrategia de UUIDs

- Todas las claves primarias son `UUID` generadas por `gen_random_uuid()` (integrado en PostgreSQL)
- UUID v4 — aleatorio, sin garantías de ordenamiento
- No exponer IDs enteros secuenciales en las APIs (seguridad: ataques de enumeración)
- Almacenar como tipo nativo `uuid` en PostgreSQL, `UUID` en Java

---

## 8. Patrón de Almacenamiento FHIR

```sql
-- Almacén genérico de recursos FHIR
CREATE TABLE fhir_resources (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL REFERENCES tenants(id),
    fhir_id         VARCHAR(64) NOT NULL,     -- ID lógico del recurso FHIR
    resource_type   VARCHAR(50) NOT NULL,     -- Patient, Appointment, Encounter, etc.
    version_id      INTEGER     NOT NULL DEFAULT 1,
    resource_data   JSONB       NOT NULL,     -- JSON FHIR completo
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, resource_type, fhir_id)
);

-- Parámetros de búsqueda consultables extraídos del JSONB
CREATE TABLE fhir_search_params (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    fhir_resource_id UUID       NOT NULL REFERENCES fhir_resources(id) ON DELETE CASCADE,
    param_name      VARCHAR(100) NOT NULL,    -- por ejemplo "family", "birthdate"
    param_value     TEXT        NOT NULL,
    param_type      VARCHAR(20) NOT NULL      -- string, token, date, reference
);
```

---

## 9. Patrones Prohibidos

| Anti-patrón | Por qué está prohibido |
|---|---|
| `schema-per-tenant` | Demasiados esquemas para el VPS; complejidad en Flyway |
| Equivalente a `Base.create_all` (`spring.jpa.hibernate.ddl-auto=create`) | Flyway es la única autoridad de esquema |
| Almacenar contraseñas en texto plano | Siempre BCrypt |
| `SELECT *` en consultas personalizadas | Seleccionar solo las columnas necesarias |
| Timestamps como `TIMESTAMP` (sin zona horaria) | Siempre usar `TIMESTAMPTZ` |
| IDs numéricos como PK en tablas de negocio | Usar UUID para prevenir enumeración |
