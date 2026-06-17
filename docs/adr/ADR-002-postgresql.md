# ADR-002: PostgreSQL 16 como Base de Datos Principal

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation

---

## Contexto

Kuris necesita una base de datos que soporte: JSONB para recursos FHIR,
claves primarias UUID, multitenancy por fila (filtrado por `tenant_id`), transacciones
ACID completas, índices GIN para búsqueda en JSONB, y cumplimiento con la ley
argentina de privacidad de datos de salud (Ley 25.326).

## Decisión

Usar **PostgreSQL 16** como la única base de datos primaria.

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| MySQL 8 | Sin tipo JSONB nativo — el almacenamiento FHIR requeriría un almacén de documentos separado o un esquema complejo |
| MongoDB | Sin transacciones ACID multi-documento — crítico para la reserva atómica de turnos (slot + appointment + coverage en una sola transacción) |
| MariaDB | Misma limitación de JSONB que MySQL |
| H2 (para tests) | No equivalente a producción; se prefiere Testcontainers con PostgreSQL real |

## Consecuencias

**Positivo:**
- JSONB con índice GIN: recursos FHIR buscables sin full-scan
- Tipo UUID nativo: claves primarias + restricciones FK sin conversión
- Transacciones ACID: la reserva atómica de turnos (UC-01) es segura
- Row-Level Security (PostgreSQL RLS): capa adicional para el aislamiento de tenants
- Cumplimiento con Ley 25.326: el aislamiento de datos entre tenants es demostrable mediante planes de consulta

**Negativo / compromisos:**
- Más configuración que SQLite para desarrollo local (mitigado: Docker Compose)
- Migraciones Flyway requeridas (sin creación automática de esquema)

**Riesgos:**
- Restricción del VPS: `shared_buffers` debe limitarse a 128MB para mantenerse
  dentro del límite de memoria del contenedor de 512MB
