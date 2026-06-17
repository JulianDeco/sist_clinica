# ADR-003: Multitenancy por Fila en lugar de Esquema por Tenant

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, UC-01, UC-02, UC-03, UC-04

---

## Contexto

Kuris es una plataforma SaaS multitenant. Cada tenant (clínica) debe estar
completamente aislado: un tenant nunca debe ver los pacientes, turnos o registros
clínicos de otro tenant.

Existen dos estrategias principales: esquema por tenant (esquema PostgreSQL separado
por clínica) y por fila (todos los tenants en tablas compartidas, filtrados por columna
`tenant_id`).

## Decisión

Usar **multitenancy por fila**: todas las tablas con alcance de tenant incluyen una
columna `tenant_id UUID NOT NULL`. Cada consulta que lea o escriba datos del tenant
debe incluir `WHERE tenant_id = ?`.

Mecanismo de aplicación: `TenantContextFilter` pobla un ThreadLocal
a partir del claim `tenant_id` del JWT. La clase base `TenantAwareRepository`
aplica el filtro automáticamente a todas las consultas derivadas.

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Esquema por tenant | VPS 4GB RAM: N connection pools separados (uno por esquema) no son sostenibles para 5–20 tenants. Las migraciones Flyway requieren iterar todos los esquemas. |
| Base de datos por tenant | Impracticable en un solo VPS — requiere N instancias de PostgreSQL o N bases de datos con credenciales separadas |

## Consecuencias

**Positivo:**
- Pool de conexiones único — eficiente en un VPS con recursos limitados
- Flyway: `flyway migrate` se aplica a todos los tenants en un solo pase
- Operaciones más simples: un solo backup, una sola base de datos para monitorear
- Las consultas de inteligencia / reporte pueden agregar datos entre tenants para
  análisis de plataforma (con permiso explícito)

**Negativo / compromisos:**
- Un bug que omita `WHERE tenant_id = ?` filtraría datos entre tenants
  — mitigado por: clase base `TenantAwareRepository`, test de integración obligatorio
  para aislamiento de tenant por repositorio

**Riesgos:**
- El error del desarrollador es el riesgo principal. Mitigación: el checklist de
  revisión de código incluye un ítem explícito "tenant_id presente en todas las consultas"

## Notas

PostgreSQL Row-Level Security (RLS) puede agregarse como capa de seguridad adicional
en un ADR futuro (requiere un rol de BD separado por tenant). Para el MVP,
el filtrado a nivel de aplicación es suficiente y más simple de gestionar.
