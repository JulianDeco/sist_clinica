import base64
import hashlib
import secrets

import pytest
import pytest_asyncio
from httpx import AsyncClient

from app.core.security import create_access_token
from app.modules.auth.models import User


def make_pkce_pair() -> tuple[str, str]:
    verifier = secrets.token_urlsafe(32)
    digest = hashlib.sha256(verifier.encode()).digest()
    challenge = base64.urlsafe_b64encode(digest).rstrip(b"=").decode()
    return verifier, challenge


async def full_auth_flow(client: AsyncClient, user: User) -> tuple[str, str]:
    verifier, challenge = make_pkce_pair()
    resp = await client.get(
        "/oauth/authorize",
        params={
            "response_type": "code",
            "client_id": "clinica-frontend",
            "redirect_uri": "http://localhost:3000/callback",
            "code_challenge": challenge,
            "code_challenge_method": "S256",
            "email": user.email,
            "password": "password123",
        },
        follow_redirects=False,
    )
    assert resp.status_code == 307
    location = resp.headers["location"]
    code = location.split("code=")[1].split("&")[0]

    resp = await client.post(
        "/oauth/token",
        data={
            "grant_type": "authorization_code",
            "code": code,
            "code_verifier": verifier,
            "redirect_uri": "http://localhost:3000/callback",
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    return data["access_token"], data["refresh_token"]


@pytest.mark.asyncio
async def test_login_correcto_retorna_tokens(client: AsyncClient, test_user: User):
    access, refresh = await full_auth_flow(client, test_user)
    assert access
    assert refresh


@pytest.mark.asyncio
async def test_login_credenciales_incorrectas_retorna_401(client: AsyncClient, test_user: User):
    verifier, challenge = make_pkce_pair()
    resp = await client.get(
        "/oauth/authorize",
        params={
            "response_type": "code",
            "client_id": "clinica-frontend",
            "redirect_uri": "http://localhost:3000/callback",
            "code_challenge": challenge,
            "code_challenge_method": "S256",
            "email": test_user.email,
            "password": "wrong_password",
        },
        follow_redirects=False,
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_jwt_manipulado_retorna_401(client: AsyncClient, test_user: User):
    access, _ = await full_auth_flow(client, test_user)
    tampered = access[:-5] + "XXXXX"
    resp = await client.post(
        "/oauth/revoke",
        data={"token": "cualquier_valor"},
        headers={"Authorization": f"Bearer {tampered}"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_refresh_token_valido_retorna_nuevo_access_token(client: AsyncClient, test_user: User):
    _, refresh = await full_auth_flow(client, test_user)
    resp = await client.post(
        "/oauth/token",
        data={"grant_type": "refresh_token", "refresh_token": refresh},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["access_token"]
    assert data["refresh_token"] != refresh


@pytest.mark.asyncio
async def test_refresh_token_revocado_retorna_401(client: AsyncClient, test_user: User):
    access, refresh = await full_auth_flow(client, test_user)

    await client.post(
        "/oauth/revoke",
        data={"token": refresh},
        headers={"Authorization": f"Bearer {access}"},
    )

    resp = await client.post(
        "/oauth/token",
        data={"grant_type": "refresh_token", "refresh_token": refresh},
    )
    assert resp.status_code == 401
