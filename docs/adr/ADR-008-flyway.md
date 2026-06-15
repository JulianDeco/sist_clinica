# ADR-008: Flyway para Migraciones de Base de Datos

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, base de datos

---

## Contexto

Los cambios de esquema de la base de datos necesitan control de versiones con un
proceso repetible y auditable. El `ddl-auto=create` o `update` de Spring Boot JPA
es peligroso en producción — puede eliminar columnas silenciosamente o fallar.

## Decisión

Usar **Flyway** como el único mecanismo de migración de esquema.

`spring.jpa.hibernate.ddl-auto=validate` (nunca `create` ni `update`).
Flyway se ejecuta al inicio de la aplicación antes de que el contexto de Spring
esté completamente inicializado.
Los archivos de migración residen en `src/main/resources/db/migration/V{N}__{desc}.sql`.

## Consecuencias

**Positivo:**
- El historial del esquema se rastrea en la tabla `flyway_schema_history`
- Una migración fallida detiene el inicio de la aplicación — evita ejecutar
  contra un esquema incorrecto
- Migraciones SQL (no Java) — legibles, revisables, sin abstracción del ORM

**Negativo / compromisos:**
- Edición Community: sin rollback automático — el rollback debe ser una nueva migración
- El desarrollador nunca debe modificar un archivo de migración ya confirmado en git

**Riesgos:**
- Migraciones grandes sobre datos de producción: deben probarse contra un volumen de datos
  representativo de producción antes de desplegar
