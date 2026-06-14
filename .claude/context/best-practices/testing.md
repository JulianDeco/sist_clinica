# Testing — Backend y Frontend

## Backend: pytest + pytest-asyncio

```python
# conftest.py — fixtures base
@pytest.fixture
async def db_session():
    # PostgreSQL de test (no SQLite — evitar divergencias con JSONB)
    async with AsyncSession(test_engine) as session:
        yield session
        await session.rollback()

@pytest.fixture
async def client(db_session):
    async with AsyncClient(app=app, base_url="http://test") as ac:
        yield ac

@pytest.fixture
async def auth_headers(client):
    # Login y retornar headers con Bearer token
    response = await client.post("/oauth/token", data={...})
    token = response.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}
```

## Qué testear en backend

| Qué | Cómo |
|---|---|
| Endpoint retorna status correcto | `assert response.status_code == 200` |
| JWT inválido rechazado | request sin token / token manipulado → 401 |
| Permisos insuficientes | token de recepcionista en endpoint de médico → 403 |
| Validación FHIR | body con recurso malformado → 422 |
| Lógica de negocio | tope obra social → warning en body (no 4xx) |
| Sobreturno creado | no consume Slot + aparece en sobreturno_log |

## Naming tests

```python
def test_create_patient_returns_201():         # ✅ descriptivo
def test_invalid_jwt_returns_401():            # ✅
def test_recepcionista_cannot_read_encounter(): # ✅
def test_patient():                             # ❌ demasiado vago
```

## Frontend: Vitest + React Testing Library

```typescript
// Patrón estándar de test de componente
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { server } from '../mocks/server'  // msw

test('AppointmentSlot muestra badge sobreturno', () => {
  render(<AppointmentSlot appointment={mockSobreturno} />)
  expect(screen.getByText(/sobreturno/i)).toBeInTheDocument()
})
```

- Mockear API con **msw** (Mock Service Worker) — no mockear fetch manualmente
- Testear comportamiento visible (texto, clicks, formularios), no implementación interna
- No testear estilos CSS

## E2E: Playwright (solo flujos críticos)

Flujos que merecen E2E:
- Login completo → dashboard
- Crear turno → aparece en agenda
- Abrir turno → crear Encounter → SOAP → receta

```typescript
// playwright.config.ts
// baseURL apunta al backend local (docker compose) para E2E
```

## Cuándo correr tests

| Evento | Tests a correr |
|---|---|
| Antes de cada commit | Tests del módulo modificado |
| Antes de abrir PR | Suite completa (`pytest` / `vitest`) |
| En CI (GitHub Actions) | Suite completa en ambos |

## NO HACER

- No usar SQLite en tests de backend (el JSONB de PostgreSQL no es compatible)
- No mockear la BD en tests de backend — usar PostgreSQL de test real
- No escribir tests que solo testean que el framework funciona
- No skipear tests rotos — arreglarlos antes del commit
