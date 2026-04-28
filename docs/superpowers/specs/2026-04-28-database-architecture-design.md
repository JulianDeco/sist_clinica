# Diseño de Arquitectura — Base de Datos y Código
**Fecha:** 2026-04-28
**Estado:** pendiente de aprobación

---

## Contexto y decisiones fundacionales

| Decisión | Elección | Razón |
|---|---|---|
| Multi-tenancy | Sí, desde el día uno | Sistema SaaS, múltiples clínicas |
| Aislamiento de datos | Row-Level Security (PostgreSQL) | Seguridad a nivel BD, datos médicos |
| Tipo de PK | UUID v7 (time-ordered) | Mejor rendimiento B-tree, sin fragmentación de índices |
| Generación UUID | Aplicación (`uuid6` Python) | Control total, PG16 no tiene UUID v7 nativo |
| FHIR storage | JSONB genérico (`fhir_resources`) + `fhir_search_params` | Estándar FHIR, interoperabilidad, evita full-scan |
| Módulos | Catálogo + suscripción por clínica + flags env | SaaS con contratos por módulo |
| Auditoría | Hash chaining tamper-evident + PII masking | Datos médicos, integridad inmutable |
| Arquitectura código | Layered + elementos Hexagonal donde agrega valor | Académica + pragmática |
| Punto de partida | Diseño limpio desde cero (Alembic migración inicial) | No hay datos en producción |

---

## Sección 1 — Mapa de entidades

```
clinics  ──────────────────────────────────────────────────────────────┐
  ├── clinic_settings (config k/v por clínica)                         │ clinic_id
  └── clinic_modules  (módulos contratados)                            │ en todas
                                                                        ▼
users ──── user_clinic_memberships ──── user_roles ──── roles ──── role_permissions ──► permissions
  │              (clínica)              (directo)     (por clínica)                     (global)
  │                                        │
  │                                   user_groups ──── groups ──── group_roles
  │                                                   (por clínica)
  └── user_clinic_permissions (grants/denials directos)
  └── is_staff → dashboard plataforma (bypass RLS)

refresh_tokens / authorization_codes  (scoped a clinic_id)

modules            ──── module_dependencies  (catálogo global, staff)
role_roles         (herencia transitiva de roles)
permissions        (catálogo global, sin RLS)
cie10_codes        (catálogo global, sin RLS)

fhir_resources     (JSONB, RLS) ──── fhir_search_params (índice búsqueda, RLS)
  └── Patient, Practitioner, Schedule, Slot, Appointment,
      Encounter, Condition, MedicationRequest, Coverage,
      FamilyMemberHistory, ChargeItem

obra_social                    (RLS)
practitioner_obra_social_config (RLS)
sobreturno_log                  (RLS)
family_history_features         (RLS)
notification_templates          (RLS)
notification_queue              (RLS)
audit_logs                      (RLS, inmutable, hash chaining)
```

---

## Sección 2 — Tablas core: identidad, clínicas y auth

```sql
clinics (
  id          UUID v7 PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  slug        VARCHAR(100) UNIQUE NOT NULL,
  is_active   BOOLEAN DEFAULT TRUE,
  created_at  TIMESTAMPTZ DEFAULT now()
)

users (
  id               UUID v7 PRIMARY KEY,
  email            VARCHAR(255) UNIQUE NOT NULL,
  hashed_password  TEXT NOT NULL,
  is_active        BOOLEAN DEFAULT TRUE,
  is_staff         BOOLEAN DEFAULT FALSE,  -- acceso a dashboard plataforma (bypass RLS)
  created_at       TIMESTAMPTZ DEFAULT now()
)

user_clinic_memberships (
  id         UUID v7 PRIMARY KEY,
  user_id    UUID NOT NULL REFERENCES users(id),
  clinic_id  UUID NOT NULL REFERENCES clinics(id),
  is_active  BOOLEAN DEFAULT TRUE,
  joined_at  TIMESTAMPTZ DEFAULT now(),
  UNIQUE (user_id, clinic_id)
)
-- RLS: clinic_id

refresh_tokens (
  id          UUID v7 PRIMARY KEY,
  user_id     UUID NOT NULL REFERENCES users(id),
  clinic_id   UUID NOT NULL REFERENCES clinics(id),
  token_hash  TEXT UNIQUE NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN DEFAULT FALSE,
  created_at  TIMESTAMPTZ DEFAULT now()
)
-- RLS: clinic_id

authorization_codes (
  id              UUID v7 PRIMARY KEY,
  code            TEXT UNIQUE NOT NULL,
  user_id         UUID NOT NULL REFERENCES users(id),
  clinic_id       UUID NOT NULL REFERENCES clinics(id),
  code_challenge  TEXT NOT NULL,
  redirect_uri    TEXT NOT NULL,
  expires_at      TIMESTAMPTZ NOT NULL,
  used            BOOLEAN DEFAULT FALSE
)
-- RLS: clinic_id
```

**Niveles de acceso:**
- `is_staff = TRUE` → dashboard plataforma, gestión de clínicas, bypass RLS
- clinic `super_admin` → admin dentro de su clínica
- usuarios normales → médico, recepcionista, etc.

---

## Sección 3 — Módulos del sistema

```sql
modules (
  id            UUID v7 PRIMARY KEY,
  name          VARCHAR(100) UNIQUE NOT NULL,
  display_name  VARCHAR(255) NOT NULL,
  description   TEXT,
  version       VARCHAR(20) DEFAULT '1.0.0',
  is_active     BOOLEAN DEFAULT TRUE,
  created_at    TIMESTAMPTZ DEFAULT now()
)
-- Sin RLS — catálogo global gestionado por staff

module_dependencies (
  module_id           UUID NOT NULL REFERENCES modules(id),
  requires_module_id  UUID NOT NULL REFERENCES modules(id),
  PRIMARY KEY (module_id, requires_module_id)
)

clinic_modules (
  id             UUID v7 PRIMARY KEY,
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  module_id      UUID NOT NULL REFERENCES modules(id),
  is_enabled     BOOLEAN DEFAULT TRUE,
  contracted_at  TIMESTAMPTZ DEFAULT now(),
  expires_at     TIMESTAMPTZ,
  config         JSONB,
  UNIQUE (clinic_id, module_id)
)
-- RLS: clinic_id
```

**Árbol de dependencias:**
```
core (siempre activo)
  └── pacientes
        └── profesionales
              ├── agenda
              │     ├── historial
              │     ├── obra_social
              │     └── notificaciones
              └── reportes
  └── stock (Fase futura)
```

**Resolución de módulo activo:**
1. `modules.is_active` = kill switch global (staff)
2. `clinic_modules` EXISTS + `is_enabled = TRUE` = contratado y activo
3. `MODULES_FORCE_DISABLED` en `.env` = override global, ninguna BD puede reactivar

---

## Sección 4 — RBAC: roles, grupos, herencia y permisos directos

```sql
permissions (
  id           UUID v7 PRIMARY KEY,
  module_id    UUID REFERENCES modules(id),
  resource     VARCHAR(100) NOT NULL,
  action       VARCHAR(50) NOT NULL,  -- 'read' | 'write' | 'delete' | '*'
  description  TEXT,
  UNIQUE (resource, action)
)
-- Sin RLS — catálogo global

roles (
  id           UUID v7 PRIMARY KEY,
  clinic_id    UUID NOT NULL REFERENCES clinics(id),
  name         VARCHAR(100) NOT NULL,
  description  TEXT,
  is_system    BOOLEAN DEFAULT FALSE,  -- no borrables: super_admin, medico, recepcionista
  created_at   TIMESTAMPTZ DEFAULT now(),
  UNIQUE (clinic_id, name)
)
-- RLS: clinic_id

role_roles (
  parent_role_id  UUID NOT NULL REFERENCES roles(id),
  child_role_id   UUID NOT NULL REFERENCES roles(id),
  PRIMARY KEY (parent_role_id, child_role_id),
  CHECK (parent_role_id != child_role_id)
)
-- Ciclos detectados a nivel aplicación (BFS antes de insertar)

role_permissions (
  role_id        UUID NOT NULL REFERENCES roles(id),
  permission_id  UUID NOT NULL REFERENCES permissions(id),
  PRIMARY KEY (role_id, permission_id)
)

groups (
  id           UUID v7 PRIMARY KEY,
  clinic_id    UUID NOT NULL REFERENCES clinics(id),
  name         VARCHAR(100) NOT NULL,
  description  TEXT,
  is_system    BOOLEAN DEFAULT FALSE,  -- no borrables: Médicos, Recepcionistas, Administradores
  created_at   TIMESTAMPTZ DEFAULT now(),
  UNIQUE (clinic_id, name)
)
-- RLS: clinic_id

group_roles (
  group_id   UUID NOT NULL REFERENCES groups(id),
  role_id    UUID NOT NULL REFERENCES roles(id),
  clinic_id  UUID NOT NULL REFERENCES clinics(id),
  PRIMARY KEY (group_id, role_id)
)
-- RLS: clinic_id (denormalizado)

user_groups (
  user_id    UUID NOT NULL REFERENCES users(id),
  group_id   UUID NOT NULL REFERENCES groups(id),
  clinic_id  UUID NOT NULL REFERENCES clinics(id),
  joined_at  TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (user_id, group_id)
)
-- RLS: clinic_id (denormalizado)

user_roles (
  user_id      UUID NOT NULL REFERENCES users(id),
  role_id      UUID NOT NULL REFERENCES roles(id),
  clinic_id    UUID NOT NULL REFERENCES clinics(id),
  assigned_at  TIMESTAMPTZ DEFAULT now(),
  assigned_by  UUID REFERENCES users(id),
  PRIMARY KEY (user_id, role_id, clinic_id)
)
-- RLS: clinic_id

user_clinic_permissions (
  id             UUID v7 PRIMARY KEY,
  user_id        UUID NOT NULL REFERENCES users(id),
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  permission_id  UUID NOT NULL REFERENCES permissions(id),
  is_granted     BOOLEAN NOT NULL,  -- TRUE = grant extra, FALSE = deny explícito
  granted_by     UUID REFERENCES users(id),
  granted_at     TIMESTAMPTZ DEFAULT now(),
  UNIQUE (user_id, clinic_id, permission_id)
)
-- RLS: clinic_id
```

**Algoritmo de permisos efectivos:**
```
1. Roles directos del usuario en la clínica        (user_roles)
2. Roles de sus grupos en la clínica               (user_groups → group_roles)
3. Expandir herencia transitiva                    (BFS sobre role_roles)
4. Permisos del set de roles expandidos            (role_permissions)
5. Aplicar overrides directos:
     + user_clinic_permissions WHERE is_granted = TRUE
     - user_clinic_permissions WHERE is_granted = FALSE
6. Filtrar: solo permisos de módulos activos en la clínica
→ Cache Redis: permissions:{user_id}:{clinic_id}  TTL 5 min
```

**Defaults al crear clínica:**
```
Roles sistema:   super_admin → (*,*)
                 medico      → patient.rw, appointment.rw, encounter.w,
                               condition.w, medication_request.w, family_history.rw
                 recepcionista → patient.rw, appointment.rw

Grupos sistema:  Administradores → rol super_admin
                 Médicos         → rol medico
                 Recepcionistas  → rol recepcionista
```

---

## Sección 5 — FHIR storage + tablas de negocio

```sql
fhir_resources (
  id             UUID v7 PRIMARY KEY,
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  resource_type  VARCHAR(50) NOT NULL,
  fhir_id        VARCHAR(64) NOT NULL,
  data           JSONB NOT NULL,
  version_id     INTEGER DEFAULT 1,       -- FHIR _history
  is_deleted     BOOLEAN DEFAULT FALSE,   -- soft delete FHIR
  created_at     TIMESTAMPTZ DEFAULT now(),
  updated_at     TIMESTAMPTZ DEFAULT now(),
  created_by     UUID REFERENCES users(id),
  updated_by     UUID REFERENCES users(id),
  UNIQUE (clinic_id, resource_type, fhir_id)
)
-- RLS: clinic_id
-- INDEX (clinic_id, resource_type)
-- INDEX (clinic_id, fhir_id)
-- INDEX (clinic_id, resource_type, is_deleted)

fhir_search_params (
  id             UUID v7 PRIMARY KEY,
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  resource_id    UUID NOT NULL REFERENCES fhir_resources(id) ON DELETE CASCADE,
  resource_type  VARCHAR(50) NOT NULL,
  param_name     VARCHAR(100) NOT NULL,
  param_value    TEXT NOT NULL,
  param_type     VARCHAR(20) DEFAULT 'token'
)
-- RLS: clinic_id
-- INDEX (clinic_id, resource_type, param_name, param_value)
-- INDEX (resource_id)

obra_social (
  id           UUID v7 PRIMARY KEY,
  clinic_id    UUID NOT NULL REFERENCES clinics(id),
  nombre       VARCHAR(255) NOT NULL,
  codigo       VARCHAR(50) NOT NULL,
  descripcion  TEXT,
  is_active    BOOLEAN DEFAULT TRUE,
  UNIQUE (clinic_id, codigo)
)
-- RLS: clinic_id

practitioner_obra_social_config (
  id                 UUID v7 PRIMARY KEY,
  clinic_id          UUID NOT NULL REFERENCES clinics(id),
  practitioner_id    VARCHAR(64) NOT NULL,  -- fhir_id, no FK directa
  obra_social_id     UUID NOT NULL REFERENCES obra_social(id),
  tope_porcentaje    NUMERIC(5,2) NOT NULL,
  UNIQUE (clinic_id, practitioner_id, obra_social_id)
)
-- RLS: clinic_id

sobreturno_log (
  id                   UUID v7 PRIMARY KEY,
  clinic_id            UUID NOT NULL REFERENCES clinics(id),
  appointment_fhir_id  VARCHAR(64) NOT NULL,
  suggested_by         VARCHAR(20) NOT NULL CHECK (suggested_by IN ('manual', 'ai')),
  ai_confidence        NUMERIC(4,3) CHECK (ai_confidence BETWEEN 0 AND 1),
  approved_by          UUID REFERENCES users(id),
  decision             VARCHAR(20) CHECK (decision IN ('approved', 'rejected')),
  created_at           TIMESTAMPTZ DEFAULT now()
)
-- RLS: clinic_id

cie10_codes (
  code         VARCHAR(10) PRIMARY KEY,
  description  TEXT NOT NULL,
  chapter      VARCHAR(10),
  is_active    BOOLEAN DEFAULT TRUE
)
-- Sin clinic_id, sin RLS — catálogo global
-- INDEX (description)

family_history_features (
  id               UUID v7 PRIMARY KEY,
  clinic_id        UUID NOT NULL REFERENCES clinics(id),
  patient_fhir_id  VARCHAR(64) NOT NULL,
  condition_code   VARCHAR(10) NOT NULL REFERENCES cie10_codes(code),
  relation_type    VARCHAR(20) NOT NULL,
  onset_age        INTEGER,
  created_at       TIMESTAMPTZ DEFAULT now()
)
-- RLS: clinic_id
-- INDEX (clinic_id, patient_fhir_id)

clinic_settings (
  clinic_id   UUID NOT NULL REFERENCES clinics(id),
  key         VARCHAR(100) NOT NULL,
  value       TEXT NOT NULL,
  value_type  VARCHAR(20) DEFAULT 'string'
              CHECK (value_type IN ('string', 'integer', 'boolean', 'json')),
  updated_by  UUID REFERENCES users(id),
  updated_at  TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (clinic_id, key)
)
-- RLS: clinic_id
-- Keys predefinidas: timezone, appointment_duration_minutes, reminder_hours_before,
--                   logo_url, whatsapp_reminders_enabled, email_reminders_enabled
```

---

## Sección 6 — Auditoría con hash chaining + notificaciones

```sql
audit_logs (
  id           UUID v7 PRIMARY KEY,
  clinic_id    UUID REFERENCES clinics(id),  -- NULL = evento de plataforma
  user_id      UUID REFERENCES users(id),
  session_id   UUID,
  action       VARCHAR(50) NOT NULL,
  entity_type  VARCHAR(100),
  entity_id    VARCHAR(64),
  changes      JSONB,                        -- PII enmascarado antes de guardar
  metadata     JSONB,                        -- {ip, user_agent, request_id, endpoint}
  severity     VARCHAR(20) DEFAULT 'info'
               CHECK (severity IN ('info', 'warning', 'critical')),
  prev_hash    VARCHAR(64),
  row_hash     VARCHAR(64) NOT NULL,         -- SHA256(prev_hash || contenido)
  created_at   TIMESTAMPTZ DEFAULT now()
)
-- RLS: clinic_id
-- INDEX (clinic_id, created_at DESC)
-- INDEX (clinic_id, entity_type, entity_id)
-- Trigger previene UPDATE/DELETE (inmutable)

notification_templates (
  id             UUID v7 PRIMARY KEY,
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  module_id      UUID REFERENCES modules(id),
  event_type     VARCHAR(100) NOT NULL,
  channel        VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'whatsapp', 'sms')),
  subject        TEXT,
  body_template  TEXT NOT NULL,             -- Jinja2
  is_active      BOOLEAN DEFAULT TRUE,
  UNIQUE (clinic_id, event_type, channel)
)
-- RLS: clinic_id

notification_queue (
  id             UUID v7 PRIMARY KEY,
  clinic_id      UUID NOT NULL REFERENCES clinics(id),
  template_id    UUID REFERENCES notification_templates(id),
  channel        VARCHAR(20) NOT NULL,
  recipient      VARCHAR(255) NOT NULL,
  recipient_id   VARCHAR(64),
  payload        JSONB NOT NULL,
  status         VARCHAR(20) DEFAULT 'pending'
                 CHECK (status IN ('pending','processing','sent','failed','cancelled')),
  scheduled_at   TIMESTAMPTZ DEFAULT now(),
  sent_at        TIMESTAMPTZ,
  retry_count    SMALLINT DEFAULT 0,
  max_retries    SMALLINT DEFAULT 3,
  error_message  TEXT,
  created_at     TIMESTAMPTZ DEFAULT now()
)
-- RLS: clinic_id
-- INDEX (status, scheduled_at) WHERE status = 'pending'
-- INDEX (clinic_id, recipient_id)
```

**Hash chaining:**
```
row_hash = SHA256(prev_hash || entity_type || entity_id || changes || created_at)
prev_hash del primer registro = "0000...0000" (génesis)
Cadena rota = manipulación detectada
```

**PII masking rules:**
```python
PII_MASK_RULES = {
    "email": lambda v: v[0] + "***@" + v.split("@")[-1],
    "phone": lambda v: v[:-4].replace(...) + v[-4:],
    "dni":   lambda v: "*" * (len(v)-3) + v[-3:],
    "name":  lambda v: v[0] + "***",
}
```

---

## Sección 7 — RLS, UUID v7 y configuración

### UUID v7

```python
# pip install uuid6
import uuid6

def new_uuid() -> uuid.UUID:
    return uuid6.uuid7()

class TenantBase(Base):
    __abstract__ = True
    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=new_uuid)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(UTC))
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))
```

### RLS con ContextVars + SQLAlchemy async

```python
# context.py
_clinic_id_ctx: ContextVar[str | None] = ContextVar('clinic_id', default=None)
_is_staff_ctx:  ContextVar[bool]        = ContextVar('is_staff', default=False)

# database.py — get_db abre transacción explícita antes de SET LOCAL
# SET LOCAL solo tiene efecto dentro de una transacción activa
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        async with session.begin():          # transacción explícita — commit al salir
            if is_staff():
                await session.execute(text("SET LOCAL app.is_staff = 'true'"))
            elif get_clinic_id():
                await session.execute(
                    text("SET LOCAL app.current_clinic_id = :id"),
                    {"id": get_clinic_id()}
                )
            yield session
```

**Políticas RLS (primera migración Alembic):**
```sql
CREATE POLICY clinic_isolation ON <tabla>
  USING (
    clinic_id = current_setting('app.current_clinic_id', TRUE)::uuid
    OR current_setting('app.is_staff', TRUE) = 'true'
  );
ALTER TABLE <tabla> ENABLE ROW LEVEL SECURITY;
ALTER TABLE <tabla> FORCE ROW LEVEL SECURITY;
```

### config.py

```python
class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_nested_delimiter="__", extra="ignore")

    ENVIRONMENT: Literal["development", "staging", "production"] = "development"
    DEBUG:       bool = False
    BASE_URL:    str  = "http://localhost"

    DB:            DatabaseSettings     = DatabaseSettings()
    REDIS:         RedisSettings        = RedisSettings()
    AUTH:          AuthSettings         = AuthSettings()
    NOTIFICATIONS: NotificationSettings = NotificationSettings()

    MODULES_DEFAULT_ENABLED: list[str] = ["core", "pacientes", "profesionales", "agenda"]
    MODULES_FORCE_DISABLED:  list[str] = []
```

Env vars con `__` como separador: `DB__URL`, `REDIS__PERMISSIONS_TTL`, `AUTH__SECRET_KEY`, etc.

---

## Sección 8 — Arquitectura de código (Layered + Hexagonal)

### Estructura de directorios

```
backend/app/
├── core/
│   ├── config.py          # Settings por dominio
│   ├── database.py        # Engine, TenantBase, get_db con RLS
│   ├── context.py         # ContextVars: clinic_id, is_staff
│   ├── dependencies.py    # get_current_user, require_permission, get_current_clinic
│   ├── security.py        # JWT: generar/verificar
│   ├── redis.py           # RedisClient singleton
│   ├── exceptions.py      # Excepciones de dominio tipadas
│   ├── events.py          # EventBus, DomainEvent, EventObserver ABC
│   └── module_registry.py # ModuleRegistry, BaseModule ABC
│
├── modules/
│   ├── auth/              # router, models, schemas, service, repository
│   ├── rbac/              # router, models, schemas, service, repository
│   ├── clinics/           # router, models, schemas, service, repository (staff)
│   ├── obra_social/
│   ├── notificaciones/
│   └── audit/             # models, service, repository, observer
│
├── fhir/
│   ├── router.py
│   ├── capability.py
│   ├── search.py          # FHIRSearchParser
│   ├── repository.py      # FHIRRepository (clase base)
│   └── resources/         # Un archivo por recurso FHIR
│
├── infrastructure/
│   ├── ports/
│   │   ├── notification_port.py   # ABC NotificationPort
│   │   └── cache_port.py          # ABC CachePort
│   └── adapters/
│       ├── email_adapter.py       # EmailAdapter(NotificationPort)
│       ├── whatsapp_adapter.py    # WhatsAppAdapter(NotificationPort)
│       └── redis_adapter.py       # RedisAdapter(CachePort)
│
└── main.py                # FastAPI app, registro de módulos, lifespan
```

### Patrones aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| Repository | `modules/*/repository.py` | Abstraer acceso a BD detrás de interfaz |
| Service Layer | `modules/*/service.py` | Lógica de negocio en clases, routers solo coordinan |
| Strategy | `infrastructure/adapters/` | Canales de notificación intercambiables |
| Observer | `modules/audit/observer.py`, `modules/notificaciones/observer.py` | Auditoría y notifs desacopladas del dominio |
| Module Registry | `core/module_registry.py` | Registrar módulos sin modificar código existente |
| Port & Adapter | `infrastructure/ports/`, `infrastructure/adapters/` | Notificaciones y cache como puertos intercambiables |
| Dependency Injection | `core/dependencies.py` + FastAPI `Depends()` | Wiring de servicios sin acoplamiento |

### Flujo de una request

```
POST /fhir/R4/Appointment
  → [RLS Middleware] setea clinic_id en ContextVar → SET LOCAL en sesión BD
  → [ModuleGuard] verifica módulo 'agenda' activo en clínica
  → [require_permission("appointment", "write")] Redis → BD fallback
  → AppointmentRouter → AppointmentService
       ├── ObraSocialService.check_tope()
       ├── FHIRRepository.save() → fhir_resources + fhir_search_params
       └── EventBus.publish(AppointmentCreatedEvent)
             ├── AuditObserver → mask PII + hash chain + insert audit_log
             └── NotificationObserver → enqueue notification_queue
```

---

## Tablas que NO tienen RLS (catálogos globales)

| Tabla | Razón |
|---|---|
| `modules` | Gestionado por staff, compartido entre clínicas |
| `module_dependencies` | Catálogo de dependencias global |
| `permissions` | Catálogo global de permisos del sistema |
| `cie10_codes` | ~17.000 códigos compartidos |

---

## Redis — keys del sistema

| Key | TTL | Contenido |
|---|---|---|
| `login_ticket:{ticket}` | 120 s | `user_id` (flujo OAuth2 authorize) |
| `permissions:{user_id}:{clinic_id}` | 5 min | JSON list de `{resource, action}` |
| `tope:{practitioner_id}:{obra_social_id}:{iso_week}` | 1 hora | porcentaje float |

---

## Decisiones pendientes para la implementación

- Librería de worker async para `notification_queue`: **ARQ** (recomendado, basado en Redis, encaja con el stack) vs Celery
- Proveedor WhatsApp: Twilio vs Meta WhatsApp Business API directa
- RS256 vs HS256 para JWT a futuro (si se agregan clientes externos)
- Soporte `_history` FHIR: implementar en Fase 2 (requiere tabla `fhir_resource_versions`)
