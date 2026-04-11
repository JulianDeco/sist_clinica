import secrets
import uuid
from datetime import UTC, datetime, timedelta

from jose import JWTError
from redis.asyncio import Redis
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.security import (
    create_access_token,
    hash_token,
    verify_password,
    verify_pkce,
)
from app.modules.auth.models import AuthorizationCode, RefreshToken, User

LOGIN_TICKET_TTL = 120  # segundos


async def create_login_ticket(redis: Redis, user_id: str) -> str:
    ticket = secrets.token_urlsafe(32)
    await redis.set(f"login_ticket:{ticket}", user_id, ex=LOGIN_TICKET_TTL)
    return ticket


async def consume_login_ticket(redis: Redis, ticket: str) -> str | None:
    """Retorna user_id y elimina el ticket (uso único)."""
    key = f"login_ticket:{ticket}"
    user_id = await redis.get(key)
    if user_id:
        await redis.delete(key)
    return user_id


async def get_user_by_email(db: AsyncSession, email: str) -> User | None:
    result = await db.execute(select(User).where(User.email == email))
    return result.scalar_one_or_none()


async def authenticate_user(db: AsyncSession, email: str, password: str) -> User | None:
    user = await get_user_by_email(db, email)
    if not user or not user.is_active:
        return None
    if not verify_password(password, user.hashed_password):
        return None
    return user


async def create_authorization_code(
    db: AsyncSession, user: User, code_challenge: str, redirect_uri: str
) -> str:
    code = secrets.token_urlsafe(32)
    auth_code = AuthorizationCode(
        code=code,
        user_id=user.id,
        code_challenge=code_challenge,
        redirect_uri=redirect_uri,
        expires_at=datetime.now(UTC) + timedelta(minutes=5),
    )
    db.add(auth_code)
    await db.commit()
    return code


async def exchange_code_for_tokens(
    db: AsyncSession, code: str, code_verifier: str, redirect_uri: str
) -> tuple[str, str]:
    result = await db.execute(
        select(AuthorizationCode).where(
            AuthorizationCode.code == code,
            AuthorizationCode.used == False,  # noqa: E712
            AuthorizationCode.expires_at > datetime.now(UTC),
        )
    )
    auth_code = result.scalar_one_or_none()
    if not auth_code:
        raise ValueError("Código inválido o expirado")
    if auth_code.redirect_uri != redirect_uri:
        raise ValueError("redirect_uri no coincide")
    if not verify_pkce(code_verifier, auth_code.code_challenge):
        raise ValueError("PKCE verification failed")

    auth_code.used = True
    await db.flush()

    user = await db.get(User, auth_code.user_id)
    access_token = create_access_token(user.id, user.role_name)
    refresh_token = await _create_refresh_token(db, user.id)
    await db.commit()
    return access_token, refresh_token


async def rotate_refresh_token(
    db: AsyncSession, refresh_token: str
) -> tuple[str, str]:
    token_hash = hash_token(refresh_token)
    result = await db.execute(
        select(RefreshToken).where(
            RefreshToken.token_hash == token_hash,
            RefreshToken.revoked == False,  # noqa: E712
            RefreshToken.expires_at > datetime.now(UTC),
        )
    )
    stored = result.scalar_one_or_none()
    if not stored:
        raise ValueError("Refresh token inválido o expirado")

    stored.revoked = True
    await db.flush()

    user = await db.get(User, stored.user_id)
    access_token = create_access_token(user.id, user.role_name)
    new_refresh = await _create_refresh_token(db, user.id)
    await db.commit()
    return access_token, new_refresh


async def revoke_refresh_token(db: AsyncSession, refresh_token: str) -> bool:
    token_hash = hash_token(refresh_token)
    result = await db.execute(
        select(RefreshToken).where(RefreshToken.token_hash == token_hash)
    )
    stored = result.scalar_one_or_none()
    if not stored:
        return False
    stored.revoked = True
    await db.commit()
    return True


async def _create_refresh_token(db: AsyncSession, user_id: str) -> str:
    token = secrets.token_urlsafe(48)
    db.add(
        RefreshToken(
            user_id=user_id,
            token_hash=hash_token(token),
            expires_at=datetime.now(UTC) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
        )
    )
    return token
