# API Standards — REST Conventions

---

## 1. URL Naming

- Lowercase, plural nouns: `/appointments`, `/patients`, `/practitioners`
- Hyphens for multi-word resources: `/insurance-coverages`
- Hierarchical resources: `/patients/{patientId}/appointments`
- Actions that don't map to CRUD use verbs on a sub-resource:
  - `POST /appointments/{id}/cancel` (not `DELETE /appointments/{id}`)
  - `POST /appointments/{id}/confirm`
  - `POST /users/{id}/activate`
- FHIR endpoints follow the FHIR R4 spec: `/fhir/R4/{ResourceType}`

---

## 2. HTTP Methods

| Method | Semantics | Body | Idempotent |
|---|---|---|---|
| `GET` | Read resource(s) | None | Yes |
| `POST` | Create resource | Required | No |
| `PUT` | Full update | Required | Yes |
| `PATCH` | Partial update | Required (RFC 7396 Merge Patch) | No |
| `DELETE` | Soft delete | None | Yes |

---

## 3. HTTP Status Codes

| Status | When to use |
|---|---|
| `200 OK` | Successful GET, PUT, PATCH |
| `201 Created` | Successful POST (include `Location` header) |
| `204 No Content` | Successful DELETE, or PATCH with no body response |
| `400 Bad Request` | Validation error (malformed request, Bean Validation failure) |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden` | Valid token but insufficient permissions |
| `404 Not Found` | Resource does not exist (or does not belong to this tenant) |
| `409 Conflict` | Business rule violation (slot already booked, duplicate resource) |
| `422 Unprocessable Entity` | Semantically invalid but syntactically valid (e.g. past date) |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Unexpected server error |

---

## 4. Request Format

```http
POST /api/v1/appointments HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>
X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440000

{
    "slotId": "...",
    "patientId": "...",
    "notes": "Optional notes"
}
```

Rules:
- Always `Content-Type: application/json` for request bodies
- `Authorization: Bearer <token>` on every authenticated request
- `X-Tenant-ID` header is required on all tenant-scoped endpoints
- Request body uses `camelCase` property names

---

## 5. Response Format

### Success Response

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

Rules:
- `camelCase` property names
- Dates in **ISO 8601** with timezone offset: `2026-06-10T14:30:00-03:00`
- IDs always as UUID strings
- Enums as uppercase strings: `"BOOKED"`, `"CANCELLED"`
- No null fields in response — omit optional absent fields OR use explicit `null` consistently per resource (decide per resource, document in module spec)

---

## 6. Error Response Format

```json
{
    "errorCode": "SLOT_ALREADY_BOOKED",
    "message": "The requested slot is no longer available.",
    "timestamp": "2026-06-08T10:15:00-03:00",
    "path": "/api/v1/appointments",
    "traceId": "a1b2c3d4-..."
}
```

For validation errors (`400`):
```json
{
    "errorCode": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "timestamp": "2026-06-08T10:15:00-03:00",
    "path": "/api/v1/appointments",
    "traceId": "...",
    "fieldErrors": [
        { "field": "slotId", "message": "must not be null" },
        { "field": "patientId", "message": "must not be null" }
    ]
}
```

Rules:
- `errorCode` is a stable string constant (used by frontend for i18n)
- `message` is human-readable in Spanish (end-user facing)
- `traceId` enables log correlation (populated from MDC)

---

## 7. Pagination

All list endpoints return paginated responses:

**Request:**
```http
GET /api/v1/appointments?page=0&size=20&sort=date,asc
```

**Response:**
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

Rules:
- Page numbers are **zero-based** (Spring Data Pageable convention)
- Default page size: 20; maximum: 100
- Never return unbounded lists (always paginate)
- Use Spring Data `Page<T>` and `Pageable` in repository + service

---

## 8. Sorting

```http
GET /api/v1/appointments?sort=date,asc&sort=createdAt,desc
```

Rules:
- Multiple sort fields supported
- Sort field names match the **response DTO field names** (camelCase)
- Invalid sort fields return `400 Bad Request`
- Default sort documented per resource in module spec

---

## 9. Filtering

```http
GET /api/v1/appointments?date=2026-06-10&status=BOOKED&practitionerId=uuid
```

Rules:
- Filters as query parameters (not request body)
- Date filters accept ISO 8601 date: `2026-06-10`
- Range filters: `dateFrom` + `dateTo`
- Complex filters (multi-criteria) use **Spring Data Specifications**
- Filter parameters documented per endpoint in module spec

---

## 10. API Versioning

- URL-based versioning: `/api/v1/`, `/api/v2/`
- A new major version is required only for breaking changes
- Breaking change examples: removed field, renamed field, changed type
- Non-breaking additions (new optional fields, new endpoints) do not require version bump
- Old versions supported for minimum 6 months after new version release

---

## 11. FHIR Endpoints

FHIR R4 endpoints follow the FHIR REST specification exactly:

```
GET    /fhir/R4/{ResourceType}/{id}
POST   /fhir/R4/{ResourceType}
PUT    /fhir/R4/{ResourceType}/{id}
DELETE /fhir/R4/{ResourceType}/{id}
GET    /fhir/R4/{ResourceType}?{searchParams}
POST   /fhir/R4/{ResourceType}/_search
GET    /fhir/R4/metadata                          (CapabilityStatement)
```

- `Content-Type: application/fhir+json` for FHIR endpoints
- Standard FHIR `OperationOutcome` for FHIR error responses
- All FHIR resources validated against FHIR R4 profiles on write

---

## 12. Rate Limiting

| Endpoint | Limit |
|---|---|
| `POST /api/v1/auth/login` | 5 requests / minute / IP |
| `POST /api/v1/auth/refresh` | 10 requests / minute / IP |
| All other endpoints | 200 requests / minute / tenant |

Exceeded limit response: `429 Too Many Requests` with `Retry-After` header.
