# Git Flow + Conventional Commits

## Modelo de branches (Git Flow)

```
main          ← producción, solo merges desde release/* o hotfix/*
develop       ← integración, base para features
feature/*     ← una feature/tarea por branch
release/*     ← preparación de release (bump version, docs)
hotfix/*      ← fix urgente en producción
```

## Reglas de branches

- `feature/T-001-docker-compose` → branch por tarea del INDEX.md
- Nunca commitear directo a `main` ni `develop`
- Cada `feature/*` sale de `develop` y se mergea a `develop` via PR
- PRs a `main` requieren aprobación del owner (@julian) — no auto-merge

## Workflow por tarea

```
1. git checkout develop && git pull
2. git checkout -b feature/T-XXX-nombre-corto
3. [implementar función]
4. [escribir test + correr tests]
5. git add <archivos específicos>   ← nunca git add .
6. git commit -m "feat(scope): descripción"
7. git push origin feature/T-XXX-nombre-corto
8. Abrir PR → develop (no a main)
9. Solicitar review al owner
10. Merge solo con aprobación
```

## Conventional Commits — tipos permitidos

| Tipo | Cuándo usar |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `test` | Agregar o corregir tests |
| `docs` | Documentación (Markdown interno) |
| `refactor` | Refactor sin cambio de comportamiento |
| `chore` | Tareas de infra, deps, config |
| `style` | Formato, espacios (sin lógica) |

## Scopes del proyecto

`infra` `auth` `rbac` `pacientes` `agenda` `historial` `obra-social` `fhir` `frontend` `db` `ci`

## Ejemplos de commits reales del proyecto

```
feat(auth): implementar endpoint POST /oauth/token con PKCE
test(auth): tests para flujo authorization code + refresh token
feat(agenda): CRUD Appointment con detección de sobreturno
fix(rbac): invalidar cache Redis al modificar permisos de rol
docs(historial): documentar estructura SOAP en backend/docs/modulos/historial.md
chore(infra): agregar límites de memoria en docker-compose.yml
feat(pacientes): FHIR Patient CRUD con search params name y birthdate
```

## PR checklist (antes de solicitar review)

- [ ] Tests pasan (`pytest` o `vitest`)
- [ ] No hay archivos > 200 líneas sin justificación
- [ ] Tarea en INDEX.md actualizada a `✅ done`
- [ ] Contexto Claude actualizado si se agregó módulo nuevo
- [ ] Sin secrets ni .env commiteados
- [ ] `git add` con archivos específicos (no `git add .`)

## NO HACER

- No acumular múltiples features en un solo commit
- No commitear archivos generados (.pyc, __pycache__, .next/)
