# Estándares de API — Convenciones REST

---

## 1. Nomenclatura de URLs

- Sustantivos en minúsculas y plural: `/appointments`, `/patients`, `/practitioners`
- Guiones para recursos de múltiples palabras: `/insurance-coverages`
- Recursos jerárquicos: `/patients/{patientId}/appointments`
- Acciones que no mapean a CRUD usan verbos en un sub-recurso:
  - `POST /appointments/{id}/cancel` (no `DELETE /appointments/{id}`)
  - `POST /appointments/{id}/confirm`
  - `POST /users/{id}/activate`
- Los endpoints FHIR siguen la especificación FHIR R4: `/fhir/R4/{ResourceType}`

---

## 2. Métodos HTTP

| Método | Semántica | Cuerpo | Idempotente |
|---|---|---|---|
| `GET` | Leer recurso(s) | Ninguno | Sí |
| `POST` | Crear recurso | Requerido | No |
| `PUT` | Actualización completa | Requerido | Sí |
| `PATCH` | Actualización parcial | Requerido (RFC 7396 Merge Patch) | No |
| `DELETE` | Borrado lógico | Ninguno | Sí |

---

## 3. Códigos de Estado HTTP

| Estado | Cuándo usar |
|---|---|
| `200 OK` | GET, PUT, PATCH exitosos |
| `201 Created` | POST exitoso (incluir encabezado `Location`) |
| `204 No Content` | DELETE exitoso, o PATCH sin respuesta con cuerpo |
| `400 Bad Request` | Error de validación (solicitud malformada, fallo de Bean Validation) |
| `401 Unauthorized` | Token ausente o inválido |
| `403 Forbidden` | Token válido pero permisos insuficientes |
| `404 Not Found` | El recurso no existe (o no pertenece a este tenant) |
| `409 Conflict` | Violación de regla de negocio (slot ya reservado, recurso duplicado) |
| `422 Unprocessable Entity` | Semánticamente inválido pero sintácticamente válido (por ejemplo, fecha pasada) |
| `429 Too Many Requests` | Límite de tasa excedido |
| `500 Internal Server Error` | Error de servidor inesperado |

---

## 4. Formato de Solicitud

```http
POST /api/v1/appointments HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>
X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440000

{
    "slotId": "...",
    "patientId": "...",
    "notes": "Notas opcionales"
}
```

Reglas:
- Siempre `Content-Type: application/json` para cuerpos de solicitud
- `Authorization: Bearer <token>` en cada solicitud autenticada
- El encabezado `X-Tenant-ID` es requerido en todos los endpoints con alcance de tenant
- El cuerpo de la solicitud usa nombres de propiedad en `camelCase`

---

## 5. Formato de Respuesta

### Respuesta Exitosa

```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "BOOKED",
    "date": "2026-06-10T14:30:00-03:00",
    "patient": {
        "id": "...",
        "fullName": "María García"
    },
    "createdAt": "2026-06-08T10:15:00-03:00"
}
```

Reglas:
- Nombres de propiedad en `camelCase`
- Fechas en **ISO 8601** con offset de zona horaria: `2026-06-10T14:30:00-03:00`
- IDs siempre como strings UUID
- Enums como strings en mayúsculas: `"BOOKED"`, `"CANCELLED"`
- Sin campos null en la respuesta — omitir campos opcionales ausentes O usar `null`
  explícito de forma consistente por recurso (decidir por recurso, documentar en el spec
  del módulo)

---

## 6. Formato de Respuesta de Error

```json
{
    "errorCode": "SLOT_ALREADY_BOOKED",
    "message": "El slot solicitado ya no está disponible.",
    "timestamp": "2026-06-08T10:15:00-03:00",
    "path": "/api/v1/appointments",
    "traceId": "a1b2c3d4-..."
}
```

Para errores de validación (`400`):
```json
{
    "errorCode": "VALIDATION_ERROR",
    "message": "La validación de la solicitud falló.",
    "timestamp": "2026-06-08T10:15:00-03:00",
    "path": "/api/v1/appointments",
    "traceId": "...",
    "fieldErrors": [
        { "field": "slotId", "message": "no debe ser nulo" },
        { "field": "patientId", "message": "no debe ser nulo" }
    ]
}
```

Reglas:
- `errorCode` es una constante de string estable (usada por el frontend para i18n)
- `message` es legible por humanos en español (orientado al usuario final)
- `traceId` permite correlación de logs (poblado desde MDC)

---

## 7. Paginación

Todos los endpoints de lista retornan respuestas paginadas:

**Solicitud:**
```http
GET /api/v1/appointments?page=0&size=20&sort=date,asc
```

**Respuesta:**
```json
{
    "content": [ ... ],
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 87,
        "totalPages": 5
    }
}
```

Reglas:
- Los números de página son **base cero** (convención de Spring Data Pageable)
- Tamaño de página predeterminado: 20; máximo: 100
- Nunca retornar listas sin límite (siempre paginar)
- Usar `Page<T>` y `Pageable` de Spring Data en repositorio y servicio

---

## 8. Ordenamiento

```http
GET /api/v1/appointments?sort=date,asc&sort=createdAt,desc
```

Reglas:
- Múltiples campos de ordenamiento soportados
- Los nombres de campo de ordenamiento coinciden con los **nombres de campo del DTO de respuesta** (camelCase)
- Los campos de ordenamiento inválidos retornan `400 Bad Request`
- El ordenamiento predeterminado documentado por recurso en el spec del módulo

---

## 9. Filtrado

```http
GET /api/v1/appointments?date=2026-06-10&status=BOOKED&practitionerId=uuid
```

Reglas:
- Filtros como parámetros de query (no en el cuerpo de la solicitud)
- Los filtros de fecha aceptan formato ISO 8601: `2026-06-10`
- Filtros de rango: `dateFrom` + `dateTo`
- Filtros complejos (multicriteriо) usan **Spring Data Specifications**
- Parámetros de filtro documentados por endpoint en el spec del módulo

---

## 10. Versionado de la API

- Versionado basado en URL: `/api/v1/`, `/api/v2/`
- Una nueva versión mayor solo es requerida para cambios que rompen compatibilidad
- Ejemplos de cambios disruptivos: campo eliminado, campo renombrado, tipo cambiado
- Las adiciones no disruptivas (nuevos campos opcionales, nuevos endpoints) no requieren
  cambio de versión
- Las versiones antiguas se soportan por un mínimo de 6 meses tras el lanzamiento de la nueva

---

## 11. Endpoints FHIR

Los endpoints FHIR R4 siguen exactamente la especificación REST de FHIR:

```
GET    /fhir/R4/{ResourceType}/{id}
POST   /fhir/R4/{ResourceType}
PUT    /fhir/R4/{ResourceType}/{id}
DELETE /fhir/R4/{ResourceType}/{id}
GET    /fhir/R4/{ResourceType}?{searchParams}
POST   /fhir/R4/{ResourceType}/_search
GET    /fhir/R4/metadata                          (CapabilityStatement)
```

- `Content-Type: application/fhir+json` para endpoints FHIR
- `OperationOutcome` FHIR estándar para respuestas de error FHIR
- Todos los recursos FHIR son validados contra perfiles FHIR R4 en escritura

---

## 12. Rate Limiting

| Endpoint | Límite |
|---|---|
| `POST /api/v1/auth/login` | 5 solicitudes / minuto / IP |
| `POST /api/v1/auth/refresh` | 10 solicitudes / minuto / IP |
| Todos los demás endpoints | 200 solicitudes / minuto / tenant |

Respuesta al superar el límite: `429 Too Many Requests` con encabezado `Retry-After`.
