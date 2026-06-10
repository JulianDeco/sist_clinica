# Entrega académica ICONIX — T-020

Seminario de Trabajo Final UAI — entrega 16/06/2026.
Los `.puml` se renderizan con el [servidor PlantUML online](https://www.plantuml.com/plantuml).

| # Entregable (consigna) | Archivo |
|---|---|
| 1. Diagrama de casos de uso (mín. 5 `<<extend>>` + 5 `<<include>>`) | `diagramas/01-casos-de-uso.puml` (6 include / 5 extend) |
| 2. Especificación de casos de uso | `02-especificacion-casos-de-uso.md` |
| 3. Diagrama de dominio | `diagramas/02-dominio.puml` + `03-glosario-dominio.md` |
| 4. Prototipo básico | `05-prototipo.md` (capturas T-002 + bocetos en papel) |
| 5. Diagrama de arquitectura | `diagramas/07-arquitectura.puml` |
| 6. Diagramas de robustez | `diagramas/03-robustez-cu01.puml` … `06-robustez-cu04.puml` |
| 7. Diagramas de secuencia | `diagramas/08-secuencia-cu01.puml` … `11-secuencia-cu04.puml` |
| 8. Diagrama de clases | `diagramas/12-clases.puml` |
| 9. Patrones de diseño (mín. 2) | `04-patrones-de-diseno.md` (Repository, Strategy, State) |

## Trazabilidad ICONIX

```
dominio (02) → casos de uso (01) → robustez (03–06) → secuencia (08–11) → clases (12)
```

Cada diagrama lleva en su cabecera la trazabilidad hacia el artefacto
anterior (paso del curso básico → flechas / control de robustez → método).
Fuentes: `.claude/tasks/use-cases.md`, ADR-009/010/013/014,
`docs/specifications/DB-Schema.spec.md`.
