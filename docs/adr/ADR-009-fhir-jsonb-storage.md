# ADR-009: Recursos FHIR Almacenados como JSONB en PostgreSQL

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, módulo FHIR

---

## Contexto

FHIR R4 define ~150 tipos de recursos con estructura variable y extensiones.
Modelar cada uno como una tabla relacional requeriría una migración por cada nuevo
tipo de recurso y no puede manejar la extensibilidad de los perfiles FHIR.

## Decisión

Almacenar los recursos FHIR como **JSONB** en una tabla `fhir_resources` única.
Extraer los parámetros de búsqueda consultables en una tabla complementaria
`fhir_search_params` (relacional) para búsquedas eficientes sin full-scans de JSONB.

Los atributos críticos del dominio (por ejemplo, `appointment_date`, `patient_id`)
se duplican en columnas tipadas de las tablas de dominio para rendimiento en JOINs.

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Una tabla por tipo de recurso FHIR | Migración por cada nuevo recurso; no puede manejar extensiones FHIR |
| JSONB puro (sin tabla de parámetros de búsqueda) | Full-scan en cada búsqueda; rendimiento inaceptable |
| Servidor FHIR dedicado (HAPI FHIR) | Sobreingeniería para el MVP; agrega otro servicio en un VPS con 4GB RAM |

## Consecuencias

**Positivo:**
- Soporta todos los tipos de recursos FHIR R4 sin cambios de esquema
- Índice GIN en `resource_data` permite consultas JSONB eficientes
- Patrón idéntico a servidores FHIR en producción (HAPI FHIR, Azure API for FHIR)
- `fhir_search_params` provee búsqueda indexada eficiente sobre valores extraídos

**Negativo / compromisos:**
- Las actualizaciones de JSONB reescriben el documento completo (aceptable para
  el tamaño de un registro clínico)
- La validación FHIR debe hacerse en el código de la aplicación, no la aplica el esquema

**Riesgos:**
- Full-scan accidental de JSONB si el desarrollador olvida usar la tabla de parámetros
  de búsqueda: mitigado por revisión de código y requisito de EXPLAIN ANALYZE para
  nuevas consultas
