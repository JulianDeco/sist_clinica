import os
from functools import lru_cache
from pathlib import Path
from typing import Literal

from pydantic import SecretStr, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ENVIRONMENT: Literal["development", "production"] = "development"
    DEBUG: bool = False

    DATABASE_URL: SecretStr
    REDIS_URL: SecretStr
    SECRET_KEY: SecretStr

    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    BASE_URL: str = "http://localhost:8000"
    OAUTH_CLIENT_ID: str = "clinica-frontend"

    model_config = SettingsConfigDict(
        env_file_encoding="utf-8",
        case_sensitive=True,
    )

    @field_validator("SECRET_KEY")
    @classmethod
    def secret_key_min_length(cls, v: SecretStr) -> SecretStr:
        if len(v.get_secret_value()) < 32:
            raise ValueError("SECRET_KEY must be at least 32 characters")
        return v

    @field_validator("BASE_URL")
    @classmethod
    def base_url_https_in_production(cls, v: str, info) -> str:
        env = (info.data or {}).get("ENVIRONMENT", "development")
        if env == "production" and v.startswith("http://"):
            raise ValueError(
                "BASE_URL must use https:// in production environment"
            )
        return v


@lru_cache
def get_settings() -> Settings:
    env = os.getenv("ENVIRONMENT", "development")
    _backend_dir = Path(__file__).parent.parent.parent
    env_file = str(_backend_dir / (".env.prod" if env == "production" else ".env.dev"))
    return Settings(_env_file=env_file)
