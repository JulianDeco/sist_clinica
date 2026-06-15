# Reglas para el Agente de IA — ClinicaSaaS

Reglas para el desarrollo asistido por IA en este proyecto. Aplican a cada
sesión de generación de código, independientemente de la feature que se esté construyendo.

---

## Antes de Generar Cualquier Código

### 1. Leer el Spec Primero

Si existe un archivo de spec para la feature, leerlo completamente antes de escribir
una sola línea de implementación. El spec es el contrato.

```
docs/modules/{module}/specs/{NombreFeature}.spec.md
```

Si no existe el spec, **no generar código de implementación**. Escribir el spec
primero y obtener su aprobación.

---

### 2. Buscar Implementaciones Existentes

Antes de crear una nueva clase, servicio o patrón:

```bash
# Encontrar patrones de servicio existentes
find src/main/java -name "*Service*.java" | head -20

# Encontrar DTOs existentes del mismo módulo
find src/main/java -path "*/agenda/dto*" -name "*.java"

# Buscar manejo de excepciones similar
grep -r "ClinicaSaasException" src/main/java/
```

Reutilizar patrones existentes. No crear una segunda abstracción para algo
que ya existe.

---

### 3. Entender la Arquitectura Existente

Leer estos archivos antes de tomar cualquier decisión estructural:

- `docs/architecture/01-high-level-architecture.md`
- `docs/standards/01-backend-standards.md` (reglas de capas)
- `docs/standards/03-database-standards.md` (reglas de migraciones)

---

## Durante la Generación de Código

### 4. Respetar la Separación de Capas

| Capa | Puede llamar a | NO puede llamar a |
|---|---|---|
| Controlador | Solo servicio de aplicación | Repositorio, entidades de dominio directamente |
| Servicio de aplicación | Repositorio, entidades de dominio, caché, notificaciones | Controlador, objetos HTTP |
| Entidad de dominio | Otros value objects | Cualquier bean de Spring |
| Repositorio | Solo JPA | Servicio de aplicación, lógica de negocio |

### 5. La Multitenancy No Es Negociable

Todo método de repositorio que lea o escriba datos del tenant debe incluir
`tenantId` como parámetro. Sin excepciones. Verificar esto en cada consulta generada.

### 6. Documentar Lo que se Genera

- Toda nueva clase `public` recibe un bloque JavaDoc a nivel de clase
- Todo nuevo método `public` recibe un bloque JavaDoc a nivel de método
- Si una decisión fue no obvia, agregar un breve comentario inline con `// WHY:`

### 7. Escribir el Test Primero

Seguir TDD: generar la clase de test con stubs de tests fallando antes de la clase
de implementación. Los nombres de método siguen:
`nombreMetodo_dadaCondicion_comportamientoEsperado`

---

## Antes de Declarar una Tarea como Completa

### 8. Verificar Contra los Casos de Prueba del Spec

Para cada TC-XX en el spec, confirmar que existe un test correspondiente que
aserta exactamente el comportamiento especificado.

### 9. Ejecutar la Suite de Tests Completa Mentalmente

Antes de reportar la finalización, recorrer:
- ¿El camino feliz funciona de extremo a extremo?
- ¿Todos los casos de error del spec están manejados?
- ¿El test de aislamiento de tenant pasa?
- ¿El umbral de cobertura se mantiene?

### 10. Verificar que la Documentación Está Completa

- [ ] Estado del spec actualizado a `IMPLEMENTED`
- [ ] JavaDoc presente en todos los elementos públicos
- [ ] Doc del módulo actualizada si la API o la máquina de estados del módulo cambió
- [ ] ADR escrito si se tomó una decisión arquitectónica significativa

---

## Qué NO Hacer

| Prohibido | Razón |
|---|---|
| Inventar reglas de negocio no presentes en el spec | Los requisitos provienen de use-cases.md y del spec |
| Agregar features "de paso" | Scope creep; crea código sin testear |
| Modificar una migración Flyway ya confirmada | Rompería la base de datos de otros desarrolladores |
| Usar `git add .` | Puede confirmar archivos .env o artefactos generados |
| Eludir fallos de tests con `--no-verify` | Los tests existen por una razón |
| Crear abstracciones para necesidades futuras hipotéticas | YAGNI — construir para lo que existe ahora |
| Eliminar una verificación de seguridad por conveniencia | Siempre preguntar antes de tocar seguridad |
| Registrar contraseñas, tokens o PII del paciente | Violación legal y de seguridad |

---

## Explicación de Decisiones

Toda elección arquitectónica o de implementación no trivial debe explicarse
en la respuesta al usuario, cubriendo:

1. **Qué** se hizo
2. **Por qué** este enfoque (sobre las alternativas)
3. **Compromisos** aceptados
4. **Riesgos** identificados

Esta explicación alimenta el ADR y el cuerpo del mensaje de commit.
