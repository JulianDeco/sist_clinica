# Estándares de Git — Estrategia de Ramas + Conventional Commits

---

## 1. Estrategia de Ramas (Git Flow)

```
main          ← producción; solo merges desde release/* o hotfix/*
develop       ← integración; base para todas las ramas de feature
feature/*     ← una tarea por rama; bifurcada desde develop
release/*     ← preparación de versión (bump de versión, changelog, tests finales)
hotfix/*      ← corrección urgente de producción; bifurcada de main, merge a main + develop
```

### Nomenclatura de Ramas

```
feature/T-{taskId}-{descripcion-corta}     feature/T-012-book-appointment-endpoint
hotfix/{descripcion-del-problema}          hotfix/jwt-expiry-bug
release/v{mayor}.{menor}.{parche}         release/v0.1.0
```

---

## 2. Convención de Commits (Conventional Commits)

```
{tipo}({alcance}): {descripción imperativa, minúsculas, sin punto}

{cuerpo opcional: por qué se hizo este cambio}

{pie opcional: referencias}
```

### Tipos

| Tipo | Cuándo |
|---|---|
| `feat` | Nueva feature |
| `fix` | Corrección de bug |
| `test` | Agregar o corregir tests |
| `docs` | Solo documentación |
| `refactor` | Cambio de código sin cambio de comportamiento |
| `chore` | Build, dependencias, configuración, CI |
| `spec` | Archivo de spec nuevo o actualizado (SDD) |
| `adr` | Nuevo Architecture Decision Record |

### Alcances

`auth` · `rbac` · `agenda` · `patients` · `clinical` · `intelligence`
`coverage` · `notifications` · `fhir` · `frontend` · `db` · `infra` · `ci`

### Ejemplos

```
feat(agenda): implement slot availability validation for UC-01
test(agenda): add integration tests for book-appointment controller
fix(auth): prevent refresh token reuse after rotation
spec(agenda): add BookAppointment spec file
adr: add ADR-009 documenting appointment booking rules
docs(rbac): update module doc with permission invalidation flow
chore(infra): add memory limits to docker-compose services
refactor(fhir): extract FhirResourceMapper to dedicated class
```

---

## 3. Workflow por Tarea

```
1.  git checkout develop && git pull
2.  git checkout -b feature/T-XXX-nombre-corto
3.  Escribir archivo de spec (si es nueva feature)  ← OBLIGATORIO antes del código
4.  Escribir tests fallando (TDD)
5.  Implementar hasta que los tests pasen
6.  Actualizar la documentación del módulo
7.  git add {archivos específicos}          ← NUNCA git add .
8.  git commit -m "..."
9.  git push origin feature/T-XXX-nombre-corto
10. Abrir PR → develop
11. Solicitar revisión
12. Merge solo con aprobación
```

**Frecuencia de commits**: al menos un commit por paso lógico (test escrito,
feature implementada, docs actualizadas). No acumular una feature entera en un solo commit.

---

## 4. Template de Pull Request

```markdown
## Resumen
<!-- ¿Qué hace este PR? Un párrafo. -->

## Tarea Relacionada
<!-- Enlace a T-XXX -->

## Spec
<!-- Enlace al archivo de spec, o N/A -->

## Tipo de Cambio
- [ ] Nueva feature
- [ ] Corrección de bug
- [ ] Refactor
- [ ] Documentación
- [ ] Infraestructura / configuración

## Testing
- [ ] Tests unitarios escritos y pasando
- [ ] Tests de integración escritos y pasando
- [ ] Umbral de cobertura mantenido (80% backend / 75% frontend)

## Documentación
- [ ] Archivo de spec escrito (si es nueva feature)
- [ ] Doc del módulo actualizada
- [ ] ADR creado (si hay decisión arquitectónica significativa)
- [ ] JavaDoc en todas las nuevas clases y métodos públicos

## Checklist
- [ ] Tests pasan localmente
- [ ] Sin secretos ni archivos .env confirmados
- [ ] Ningún archivo supera 200 líneas sin justificación
- [ ] git add con archivos específicos (no git add .)
- [ ] Los mensajes de commit siguen Conventional Commits
- [ ] El PR apunta a develop (no a main)
```

---

## 5. Checklist de Revisión de Código

Responsabilidades del revisor antes de aprobar:

**Corrección**
- [ ] La lógica coincide con las reglas de negocio del spec
- [ ] Casos límite y rutas de error manejados
- [ ] Sin condiciones de carrera en rutas concurrentes

**Arquitectura**
- [ ] Separación correcta de capas (sin lógica de negocio en controladores)
- [ ] tenant_id presente en todas las consultas de repositorio
- [ ] Sin nuevas abstracciones sin justificación

**Seguridad**
- [ ] Sin secretos codificados directamente ni registrados
- [ ] Entrada validada en el límite HTTP
- [ ] Verificación de auth/permiso presente en nuevos endpoints

**Tests**
- [ ] Los tests cubren todos los casos de prueba del spec
- [ ] El test de integración incluye verificación de aislamiento de tenant
- [ ] Los nombres de test describen el comportamiento (no la implementación)

**Documentación**
- [ ] JavaDoc en todas las clases y métodos públicos
- [ ] Archivo de spec presente (si es nueva feature)
- [ ] Doc del módulo actualizada (si el módulo cambió)

---

## 6. Reglas

- Nunca confirmar directamente en `main` ni en `develop`
- Nunca hacer force-push a `develop` ni a `main`
- Los PRs a `main` requieren aprobación de @julian
- Todo PR tiene una tarea vinculada (T-XXX)
- Eliminar las ramas de feature tras el merge
- Sin `--no-verify` para saltear hooks — corregir la falla del hook en su lugar
