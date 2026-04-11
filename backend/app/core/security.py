import base64
import hashlib
from datetime import UTC, datetime, timedelta

import bcrypt
from jose import JWTError, jwt

from app.core.config import settings


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()


def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(plain.encode(), hashed.encode())


def create_access_token(user_id: str, role_name: str) -> str:
    now = datetime.now(UTC)
    payload = {
        "sub": user_id,
        "role_name": role_name,
        "iat": now,
        "exp": now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
    }
    return jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def decode_access_token(token: str) -> dict:
    """Verifica firma y retorna payload. Lanza JWTError si inválido."""
    return jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])


def verify_pkce(code_verifier: str, code_challenge: str) -> bool:
    """Verifica code_verifier contra code_challenge (S256)."""
    digest = hashlib.sha256(code_verifier.encode()).digest()
    computed = base64.urlsafe_b64encode(digest).rstrip(b"=").decode()
    return computed == code_challenge


def hash_token(token: str) -> str:
    """Almacena solo el hash del refresh token en BD."""
    return hashlib.sha256(token.encode()).hexdigest()
