from fastapi import APIRouter, Depends, Form, HTTPException, Query, status
from fastapi.responses import RedirectResponse
from redis.asyncio import Redis
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.core.redis import get_redis
from app.modules.auth import service
from app.modules.auth.models import User

router = APIRouter(prefix="/oauth", tags=["auth"])


@router.post("/login")
async def login(
    email: str = Form(...),
    password: str = Form(...),
    db: AsyncSession = Depends(get_db),
    redis: Redis = Depends(get_redis),
):
    """Valida credenciales y retorna un login_ticket de vida corta (120 s)."""
    user = await service.authenticate_user(db, email, password)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Credenciales inválidas")
    ticket = await service.create_login_ticket(redis, str(user.id))
    return {"login_ticket": ticket, "expires_in": service.LOGIN_TICKET_TTL}


@router.get("/authorize")
async def authorize(
    response_type: str = Query(...),
    client_id: str = Query(...),
    redirect_uri: str = Query(...),
    code_challenge: str = Query(...),
    code_challenge_method: str = Query("S256"),
    state: str = Query(""),
    login_ticket: str = Query(..., description="Ticket obtenido de POST /oauth/login"),
    db: AsyncSession = Depends(get_db),
    redis: Redis = Depends(get_redis),
):
    if response_type != "code" or code_challenge_method != "S256":
        raise HTTPException(status_code=400, detail="Parámetros OAuth2 inválidos")

    user_id = await service.consume_login_ticket(redis, login_ticket)
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Login ticket inválido o expirado")

    user = await db.get(User, user_id)
    if not user or not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Usuario inactivo")

    code = await service.create_authorization_code(db, user, code_challenge, redirect_uri)
    separator = "&" if "?" in redirect_uri else "?"
    return RedirectResponse(f"{redirect_uri}{separator}code={code}&state={state}")


@router.post("/token")
async def token(
    grant_type: str = Form(...),
    code: str = Form(None),
    code_verifier: str = Form(None),
    redirect_uri: str = Form(None),
    refresh_token: str = Form(None),
    db: AsyncSession = Depends(get_db),
):
    if grant_type == "authorization_code":
        if not all([code, code_verifier, redirect_uri]):
            raise HTTPException(status_code=400, detail="Faltan parámetros")
        try:
            access_token, new_refresh = await service.exchange_code_for_tokens(
                db, code, code_verifier, redirect_uri
            )
        except ValueError as e:
            raise HTTPException(status_code=400, detail=str(e))

    elif grant_type == "refresh_token":
        if not refresh_token:
            raise HTTPException(status_code=400, detail="Falta refresh_token")
        try:
            access_token, new_refresh = await service.rotate_refresh_token(db, refresh_token)
        except ValueError as e:
            raise HTTPException(status_code=401, detail=str(e))

    else:
        raise HTTPException(status_code=400, detail="grant_type no soportado")

    return {
        "access_token": access_token,
        "refresh_token": new_refresh,
        "token_type": "bearer",
        "expires_in": 1800,
    }


@router.post("/revoke", status_code=status.HTTP_204_NO_CONTENT)
async def revoke(
    token: str = Form(...),
    _current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await service.revoke_refresh_token(db, token)
