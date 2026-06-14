# Modularización — Cuándo extraer, naming, tamaños

## Regla central: extraer cuando se repite, no antes

- 1 uso → inline
- 2 usos en distintos archivos → extraer a módulo/componente compartido
- 3+ usos → extraer con nombre genérico estable

## Backend — estructura por módulo

```
modules/<nombre>/
  router.py    ← solo endpoints + validación entrada (< 150 líneas)
  models.py    ← SQLAlchemy models + schemas Pydantic
  service.py   ← lógica de negocio (puede ser > 150 líneas si es complejo)
  (tests en backend/tests/test_<nombre>.py)
```

- Si `service.py` supera 200 líneas → dividir en `service_<subtema>.py`
- No poner lógica de negocio en routers ni en modelos

## Frontend — estructura por funcionalidad

```
components/fhir/    ← componentes ligados a recursos FHIR
components/ui/      ← shadcn/ui re-exports y primitivos
app/(dashboard)/    ← páginas (composición de componentes)
lib/                ← hooks, clientes, utilidades puras
```

- Antes de crear un componente: buscar en `components/fhir/` y `components/ui/`
- Componente nuevo solo si no existe equivalente y se usará ≥ 2 veces
- Props tipadas siempre con TypeScript interface (no `any`)

## Naming

| Elemento | Convención | Ejemplo |
|---|---|---|
| Módulo backend | `snake_case` | `obra_social/`, `medication_request.py` |
| Componente React | `PascalCase` | `AppointmentSlot.tsx`, `CIE10Search.tsx` |
| Hook React | `use` + PascalCase | `usePermissions`, `useFHIRQuery` |
| Endpoint FHIR | `/fhir/R4/{ResourceType}` | `/fhir/R4/Patient` |
| Branch git | `feature/T-XXX-nombre` | `feature/T-011-appointment-crud` |
| Task file | `T-XXX.md` | `T-011.md` |

## Cuándo NO extraer

- Lógica usada en un solo lugar → mantener inline
- Abstracción que requiere más código que la duplicación que evita → no extraer
- Componentes "por si acaso se reutiliza a futuro" → esperar al segundo uso real

## Al crear módulo nuevo

Crear `XX-nombre.md` en context/modules + actualizar Routing en CLAUDE.md — **antes de escribir código**.
Ver reglas completas en CLAUDE.md § Reglas.
