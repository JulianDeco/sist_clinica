from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.core.database import Base, engine
from app.modules.auth.router import router as auth_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield


app = FastAPI(title="Sistema Médico FHIR", version="0.1.0", lifespan=lifespan)

app.include_router(auth_router)


@app.get("/fhir/R4/metadata")
async def capability_statement():
    return {"resourceType": "CapabilityStatement", "status": "active", "fhirVersion": "4.0.1"}
