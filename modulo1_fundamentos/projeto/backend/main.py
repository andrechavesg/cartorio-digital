from fastapi import FastAPI
from pydantic import BaseModel
from typing import Dict


class Citizen(BaseModel):
    name: str
    email: str


app = FastAPI(title="Cartório Digital - Fundamentos")
registrations: Dict[str, Citizen] = {}


@app.get("/health")
def health() -> Dict[str, str]:
    """Return a simple status message so we can wire the frontend later."""
    return {"status": "ok", "module": "fundamentos"}


@app.post("/citizens")
def enroll_citizen(citizen: Citizen) -> Dict[str, str]:
    """Register a citizen that will later receive a digital certificate."""
    registrations[citizen.email] = citizen
    return {"message": "Citizen registered", "email": citizen.email}


@app.get("/citizens")
def list_citizens() -> Dict[str, Dict[str, str]]:
    """List all registered citizens."""
    return {
        "citizens": {
            email: citizen.model_dump()
            for email, citizen in registrations.items()
        }
    }
