from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    DATABASE_URL: str = "postgresql+asyncpg://clinica_user:pass@localhost/clinica_db"
    REDIS_URL: str = "redis://localhost:6379/0"

    SECRET_KEY: str = "changeme-min-32-chars-secret-key!!"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    ENVIRONMENT: str = "development"
    BASE_URL: str = "http://localhost"

    OAUTH_CLIENT_ID: str = "clinica-frontend"


settings = Settings()
