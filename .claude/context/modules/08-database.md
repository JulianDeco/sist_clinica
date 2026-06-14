# Módulo: Base de Datos

## Estructura planificada

| Archivo | Rol |
|---|---|
| `backend/app/core/database.py` | `Base`, `engine`, `AsyncSessionLocal`, `get_db` dependency |
| `backend/app/modules/auth/models.py` | `User`, `RefreshToken`, `AuthorizationCode` |
| `backend/app/modules/rbac/models.py` | `Role`, `Permission`, `RolePermission`, `UserRole` |
| `backend/app/modules/agenda/models.py` | `SobreturnoLog` y modelos de agenda |
| `backend/alembic/env.py` | Config async de Alembic |
| `backend/alembic/versions/` | Migraciones ordenadas por prefijo numérico |

## Tablas principales

```
-- Identidad (por tenant)
users               id(UUID), tenant_id(UUID NOT NULL), email, hashed_password,
                    is_active, created_at
                    [UNIQUE(tenant_id, email) — el email es único dentro del tenant,
                     no globalmente]
refresh_tokens      id(UUID), tenant_id(UUID NOT NULL), user_id→users, token_hash,
                    expires_at, revoked
                    [tenant_id necesario para RLS y para queries de limpieza por tenant]
authorization_codes id(UUID), code(UNIQUE), user_id→users, code_challenge, redirect_uri,
                    expires_at(TTL máximo 10 minutos), used

-- RBAC
roles               id(UUID), tenant_id(UUID), name, description
                    [tenant_id NULL = rol global del sistema; NOT NULL = rol custom del tenant
                     UNIQUE(tenant_id, name) — misma lógica que email en users]
permissions         id(UUID), resource, action, UNIQUE(resource, action)
                    [GLOBAL — los permisos son del sistema, no del tenant]
role_permissions    role_id→roles, permission_id→permissions  [PK compuesta]
user_roles          user_id→users, role_id→roles              [PK compuesta]

-- FHIR genérica — fuente de verdad de todos los recursos FHIR (por tenant)
fhir_resources      id(UUID), tenant_id(UUID NOT NULL), resource_type, fhir_id,
                    data(JSONB), created_at, updated_at
                    [UNIQUE(tenant_id, fhir_id) — fhir_id es único dentro del tenant,
                     no globalmente; un mismo fhir_id puede existir en dos tenants distintos]
fhir_search_params  id, tenant_id(UUID NOT NULL), resource_id→fhir_resources,
                    param_name, param_value(TEXT)
                    [param_value es TEXT — los valores múltiples se insertan como filas
                     separadas, no como array; mantiene el índice composite eficiente]

-- Negocio (tablas relacionales propias, no FHIR)
obra_social                      id, nombre, codigo, descripcion
                                 [GLOBAL — sin tenant_id: OSDE, IOMA, etc. existen
                                  independientemente de la clínica]
practitioner_obra_social_config  tenant_id(UUID NOT NULL), practitioner_id, obra_social_id,
                                 tope_porcentaje  [UNIQUE(tenant_id, practitioner_id, obra_social_id)]
sobreturno_log                   id(UUID), tenant_id(UUID NOT NULL), appointment_id,
                                 suggested_by, ai_confidence, approved_by, decision
cie10_codes                      code(PK), description, chapter
                                 [GLOBAL — catálogo compartido, sin tenant_id]
family_history_features          patient_id, tenant_id(UUID NOT NULL), condition_code,
                                 relation_type, onset_age  [para ML]

-- Auditoría (por tenant)
audit_log                        id(UUID), user_id→users, tenant_id(UUID NOT NULL),
                                 resource_type, resource_id, action(ENUM: read|write|delete),
                                 ip_address(INET), timestamp
                                 [action: ENUM en PostgreSQL — no VARCHAR libre
                                  ip_address: tipo INET — no TEXT, permite validación y
                                  operadores de red nativos (/24, <<, etc.)]
```

### Nota sobre tipos de datos en `audit_log`

**`action`** — usar `ENUM` de PostgreSQL en lugar de `VARCHAR`:

```sql
CREATE TYPE audit_action AS ENUM ('read', 'write', 'delete');
ALTER TABLE audit_log ADD COLUMN action audit_action NOT NULL;
```

Ventajas: restricción de valores garantizada por la BD, almacenamiento más compacto (4 bytes vs string).

**`ip_address`** — usar tipo `INET` de PostgreSQL en lugar de `TEXT`:

```sql
ALTER TABLE audit_log ADD COLUMN ip_address INET;
```

Ventajas: validación automática, operadores de red (`<<`, `/24`), índices GiST eficientes para rangos de IP.

### Nota sobre `hashed_password`

Usar `bcrypt` con cost=12 o `argon2id`. **Nunca SHA-1, MD5 ni ningún hash de propósito general.**

### Nota sobre `authorization_codes.expires_at`

El TTL máximo es **10 minutos** desde la emisión del código. Los códigos expirados se rechazan sin importar si fueron usados o no.

### Nota sobre `fhir_search_params.param_value`

`TEXT` es suficiente. Cuando un parámetro FHIR tiene múltiples valores (ej. múltiples telefonos), se insertan como **filas separadas** en `fhir_search_params`, no como array en una sola fila. Esto mantiene el índice composite `(tenant_id, param_name, param_value)` eficiente y evita el overhead de `TEXT[]` con GIN.

## Índices

Todos los índices se crean con `CONCURRENTLY` para no bloquear tablas en producción.

### `fhir_resources` — índices críticos

```sql
-- Query más frecuente: listar recursos por tipo dentro del tenant
-- Cubre: SELECT * FROM fhir_resources WHERE tenant_id=X AND resource_type=Y
CREATE INDEX CONCURRENTLY idx_fhir_resources_tenant_type
    ON fhir_resources (tenant_id, resource_type);

-- Búsquedas JSONB arbitrarias (ej. Patient.name, Observation.code)
-- Necesario para queries tipo: data @> '{"resourceType": "Patient"}'
CREATE INDEX CONCURRENTLY idx_fhir_resources_data_gin
    ON fhir_resources USING GIN (data);

-- updated_at para sincronización incremental ($lastUpdated en FHIR)
CREATE INDEX CONCURRENTLY idx_fhir_resources_tenant_updated
    ON fhir_resources (tenant_id, updated_at DESC);
```

### `fhir_search_params` — índice composite

```sql
-- Orden correcto: tenant_id primero (filtro más selectivo en multitenant),
-- luego param_name, luego param_value para búsquedas por prefijo (LIKE 'X%')
-- Cubre: WHERE tenant_id=X AND param_name='name' AND param_value='García'
CREATE INDEX CONCURRENTLY idx_fhir_search_params_composite
    ON fhir_search_params (tenant_id, param_name, param_value);

-- FK resource_id sin índice implícito — necesario para JOINs con fhir_resources
CREATE INDEX CONCURRENTLY idx_fhir_search_params_resource_id
    ON fhir_search_params (resource_id);
```

### `users` — login

```sql
-- UNIQUE(tenant_id, email) ya crea un índice B-tree implícito — no duplicar.
-- El índice implícito cubre: WHERE tenant_id=X AND email=Y (login).
-- No crear índice adicional.
```

### `refresh_tokens` — usado en cada request autenticado

```sql
-- token_hash: búsqueda directa en cada request con refresh token
-- UNIQUE en token_hash ya crea índice implícito si se define como UNIQUE constraint.
-- Si no tiene UNIQUE constraint, crear explícitamente:
CREATE UNIQUE INDEX CONCURRENTLY idx_refresh_tokens_token_hash
    ON refresh_tokens (token_hash);

-- FK user_id — sin índice implícito en PostgreSQL
CREATE INDEX CONCURRENTLY idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);
```

### `audit_log` — reportes por tenant y fecha

```sql
-- Query de reportes: SELECT * FROM audit_log WHERE tenant_id=X AND timestamp >= Y
CREATE INDEX CONCURRENTLY idx_audit_log_tenant_timestamp
    ON audit_log (tenant_id, timestamp DESC);

-- FK user_id — para auditoría de acciones de un usuario específico
CREATE INDEX CONCURRENTLY idx_audit_log_user_id
    ON audit_log (user_id);
```

### FKs sin índice implícito — RBAC

PostgreSQL **no crea índices automáticamente en columnas FK** (solo en PK y UNIQUE). Las FKs de las tablas RBAC necesitan índices explícitos para que los JOINs sean eficientes:

```sql
-- user_roles: la PK compuesta (user_id, role_id) cubre búsquedas por user_id.
-- Agregar índice inverso para buscar todos los usuarios de un rol:
CREATE INDEX CONCURRENTLY idx_user_roles_role_id
    ON user_roles (role_id);

-- role_permissions: la PK compuesta (role_id, permission_id) cubre búsquedas por role_id.
-- Agregar índice inverso para buscar todos los roles que tienen un permiso:
CREATE INDEX CONCURRENTLY idx_role_permissions_permission_id
    ON role_permissions (permission_id);
```

### `sobreturno_log`

```sql
-- FK appointment_id — para lookup de historial de sobreturnos de un turno
CREATE INDEX CONCURRENTLY idx_sobreturno_log_appointment_id
    ON sobreturno_log (appointment_id);

-- tenant_id para queries de reportes de sobreturnos por clínica
CREATE INDEX CONCURRENTLY idx_sobreturno_log_tenant_id
    ON sobreturno_log (tenant_id);
```

## Connection Pooling — VPS 4 GB RAM

Con PostgreSQL y el backend Spring Boot (pool HikariCP) en el **mismo host** con 4 GB RAM:

### Configuración recomendada de PostgreSQL (`postgresql.conf`)

```ini
# Máximo de conexiones del servidor
max_connections = 100

# Memoria compartida para buffer pool (~25% de RAM, conservador en VPS compartido)
shared_buffers = 512MB

# Memoria de trabajo por operación de sort/hash (cuidado: por conexión activa)
work_mem = 8MB

# Checkpoint: escribir en disco gradualmente
wal_buffers = 16MB
checkpoint_completion_target = 0.9
```

### Configuración del pool (ejemplo legacy SQLAlchemy — equivalente vigente: HikariCP en `application.yml`)

> ⚠️ Snippet del stack FastAPI descartado (ADR-001). Los valores de pool (10 + 20 overflow) siguen siendo la referencia de dimensionamiento para HikariCP.

```python
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker

engine = create_async_engine(
    settings.DATABASE_URL,
    # Pool size: conexiones persistentes abiertas. Con max_connections=100
    # y un solo proceso backend, 10-15 es suficiente. Dejar margen para
    # herramientas administrativas (psql, Alembic, etc.)
    pool_size=10,
    # Conexiones adicionales bajo pico de tráfico (superan pool_size temporalmente)
    max_overflow=20,
    # Tiempo máximo esperando una conexión libre del pool antes de error
    pool_timeout=30,
    # Reciclar conexiones después de 1 hora para evitar conexiones zombi
    pool_recycle=3600,
    # Verificar conexión antes de usar (detecta conexiones caídas)
    pool_pre_ping=True,
)

# Usar async_sessionmaker en vez de crear AsyncSession directamente
AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
)
```

**Por qué `pool_size=10` y no más:** con `max_connections=100` en PostgreSQL y 4 GB de RAM, cada conexión consume ~5-10 MB de RAM del proceso postgres. Con `pool_size=10, max_overflow=20` el total máximo es 30 conexiones, dejando margen para 2-3 workers adicionales (Celery, cron) y herramientas de admin. Si se agregan múltiples workers de uvicorn, configurar **PgBouncer** en modo transaction pooling y reducir `pool_size` a 5 por worker.

## Patrones SQLAlchemy async (legacy — stack vigente: Spring Data JPA)

> ⚠️ Snippets del stack FastAPI descartado (ADR-001). En Spring Boot la sesión la gestiona JPA/Hibernate.

```python
# Sesión correcta — usar AsyncSessionLocal (sessionmaker), no AsyncSession(engine) directo
# AsyncSession(engine) NO reutiliza el pool de conexiones correctamente
async with AsyncSessionLocal() as session:
    async with session.begin():
        result = await session.execute(select(Model).where(Model.id == id))
        obj = result.scalar_one_or_none()

# get_db dependency (legacy FastAPI) — usar AsyncSessionLocal
async def get_db():
    async with AsyncSessionLocal() as session:
        yield session
        # No llamar session.close() manualmente — el context manager lo hace
```

**Por qué `AsyncSessionLocal()` y no `AsyncSession(engine)`:** `async_sessionmaker` configura el pool, `expire_on_commit=False` y opciones de la sesión una sola vez. Crear `AsyncSession(engine)` directamente en cada request ignora esa configuración y puede crear conexiones fuera del pool.

## Migraciones Alembic

```bash
# Generar migración desde modelos
alembic revision --autogenerate -m "descripcion_corta"

# Aplicar
alembic upgrade head

# Rollback una migración
alembic downgrade -1
```

- Cada migración tiene prefijo numérico: `001_`, `002_`, etc.
- Incluir seed de datos base (roles, permisos) en `001_initial_schema.py`
- Nunca editar una migración ya aplicada — crear una nueva
- Las RLS policies se agregan en migraciones via `op.execute()` (ver sección PostgreSQL RLS)
- **Siempre escribir `downgrade()`** — las migraciones sin rollback están prohibidas

### Configuración async de Alembic (`env.py`)

Alembic no puede ejecutar código async directamente — requiere un wrapper explícito:

```python
# backend/alembic/env.py
import asyncio
from logging.config import fileConfig
from sqlalchemy.ext.asyncio import create_async_engine
from alembic import context
from app.core.database import Base
from app.core.config import settings

# Importar TODOS los modelos para que Alembic los detecte en autogenerate
import app.modules.auth.models      # noqa: F401
import app.modules.rbac.models      # noqa: F401
import app.modules.agenda.models    # noqa: F401

config = context.config
fileConfig(config.config_file_name)
target_metadata = Base.metadata

def run_migrations_offline():
    context.configure(
        url=settings.DATABASE_URL,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )
    with context.begin_transaction():
        context.run_migrations()

async def run_migrations_online():
    connectable = create_async_engine(settings.DATABASE_URL)
    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)
    await connectable.dispose()

def do_run_migrations(connection):
    context.configure(connection=connection, target_metadata=target_metadata)
    with context.begin_transaction():
        context.run_migrations()

if context.is_offline_mode():
    run_migrations_offline()
else:
    asyncio.run(run_migrations_online())
```

**Punto crítico:** si un modelo no está importado en `env.py`, Alembic no lo detecta en `--autogenerate` y genera una migración vacía. Agregar el import cada vez que se crea un módulo nuevo.

## PostgreSQL RLS — Aislamiento multi-tenant

Todas las tablas con `tenant_id` tienen Row Level Security activado. La aplicación establece el tenant activo al inicio de cada transacción:

```sql
-- Al comienzo de cada transacción (en filtro/interceptor Spring)
SET LOCAL app.current_tenant = '{tenant_id}';
```

### Ejemplo de policy

```sql
ALTER TABLE fhir_resources ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON fhir_resources
    USING (tenant_id = current_setting('app.current_tenant')::UUID);
```

### Agregar policies en migraciones Alembic

```python
# En el archivo de migración (backend/alembic/versions/XXX_nombre.py)
def upgrade():
    op.execute("ALTER TABLE fhir_resources ENABLE ROW LEVEL SECURITY")
    op.execute("""
        CREATE POLICY tenant_isolation ON fhir_resources
        USING (tenant_id = current_setting('app.current_tenant')::UUID)
    """)

def downgrade():
    op.execute("DROP POLICY IF EXISTS tenant_isolation ON fhir_resources")
    op.execute("ALTER TABLE fhir_resources DISABLE ROW LEVEL SECURITY")
```

Aplicar el mismo patrón para: `users`, `fhir_search_params`, `sobreturno_log`, `family_history_features`, `practitioner_obra_social_config`, `audit_log`.

## Particionamiento — estrategia para escala

`fhir_resources` y `audit_log` crecen indefinidamente. Cuando el volumen supere **~10 millones de filas** por tabla (monitorear con `pg_relation_size`), aplicar particionamiento.

### Estrategia recomendada

**`fhir_resources`** — particionar por `tenant_id` (hash partitioning):

```sql
-- Particionamiento por hash de tenant_id — distribuye carga entre particiones fijas
-- Usar cuando hay muchos tenants con volúmenes similares
CREATE TABLE fhir_resources (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    resource_type TEXT NOT NULL,
    fhir_id TEXT NOT NULL,
    data JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
) PARTITION BY HASH (tenant_id);

-- 8 particiones iniciales — escalar a 16 cuando supere 50M filas totales
CREATE TABLE fhir_resources_p0 PARTITION OF fhir_resources FOR VALUES WITH (MODULUS 8, REMAINDER 0);
-- ... repetir para p1..p7
```

**`audit_log`** — particionar por `timestamp` (range partitioning mensual):

```sql
-- Una partición por mes — permite DROP de particiones antiguas sin VACUUM
CREATE TABLE audit_log (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- resto de columnas
) PARTITION BY RANGE (timestamp);

-- Crear particiones por mes con cron o script de mantenimiento
CREATE TABLE audit_log_2026_05 PARTITION OF audit_log
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
```

**Ventaja clave del particionamiento por rango en `audit_log`:** permite eliminar datos antiguos con `DROP TABLE audit_log_2024_01` (instantáneo) en lugar de `DELETE` (lento + genera bloat). Ideal para cumplimiento de retención de datos.

### Cuándo implementar

| Señal | Acción |
|---|---|
| `fhir_resources` > 10M filas | Planificar migración a hash partitioning |
| `audit_log` > 5M filas/mes | Activar range partitioning mensual |
| Queries lentas con `EXPLAIN ANALYZE` mostrando Seq Scan en tablas grandes | Revisar índices primero; si persiste, evaluar particionamiento |

**Implementar particionamiento via Alembic** usando `op.execute()` con las DDL statements. Requiere migración de datos existentes (ver PostgreSQL docs: `pg_partman` para automatización).

## Redis — keys del sistema

| Key | TTL | Contenido |
|---|---|---|
| `login_ticket:{ticket}` | 120 s | `user_id` (usado en flujo OAuth2 authorize) |
| `permissions:{tenant_id}:{user_id}` | 5 min | JSON list de `{resource, action}` |
| `tope:{tenant_id}:{practitioner_id}:{obra_social_id}:{iso_week}` | 1 hora | porcentaje float |

El `tenant_id` en las keys de Redis evita colisiones de permisos y topes entre clínicas distintas.

## NO HACER

- No usar UUID como string en queries — usar tipo `UUID` de SQLAlchemy
- No hacer `session.query()` (API legacy) — usar `select()` de sqlalchemy core
- No sync SQLAlchemy en contexto async — siempre `await session.execute()`
- No guardar recursos FHIR en tablas separadas por tipo — usar `fhir_resources` JSONB
- No modificar `fhir_resources` directamente para búsquedas — indexar en `fhir_search_params`
- No consultar `fhir_search_params` con `text()` e interpolación de strings — usar siempre SQLAlchemy ORM o `select()` con parámetros enlazados (previene SQL injection)
- No usar SHA-1 ni MD5 para contraseñas — solo `bcrypt` (cost≥12) o `argon2id`
- No omitir `tenant_id` en las keys de Redis — las keys sin tenant mezclan datos entre clínicas
- No crear índices sin `CONCURRENTLY` en tablas de producción — bloquea escrituras
- No usar `AsyncSession(engine)` directamente en `get_db` — usar `AsyncSessionLocal()` del sessionmaker
- No omitir `downgrade()` en migraciones — toda migración debe ser reversible
- No omitir imports de modelos en `alembic/env.py` — Alembic no detecta modelos no importados
- No usar `TEXT` para `ip_address` en `audit_log` — usar tipo `INET` de PostgreSQL
- No usar `VARCHAR` libre para `action` en `audit_log` — usar `ENUM` de PostgreSQL

## Dependencias

→ `.claude/context/modules/02-rbac.md` (modelo de roles/permisos)
→ `.claude/context/modules/03-fhir.md` (uso de fhir_resources JSONB)
