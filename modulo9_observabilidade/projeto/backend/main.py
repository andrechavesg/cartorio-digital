from typing import Dict

from fastapi import FastAPI
from pydantic import BaseModel

from observability import ObservabilityHub


class MetricRequest(BaseModel):
    name: str
    value: float
    labels: Dict[str, str] | None = None


class TraceRequest(BaseModel):
    span_id: str
    name: str
    duration_ms: float
    attributes: Dict[str, str] | None = None


class LogRequest(BaseModel):
    level: str
    message: str
    context: Dict[str, str] | None = None


app = FastAPI(title="Cartório Digital - Observabilidade")
hub = ObservabilityHub()


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "observabilidade"}


@app.post("/metrics")
def record_metric(request: MetricRequest) -> Dict[str, str]:
    labels = request.labels or {}
    sample = hub.record_metric(request.name, request.value, **labels)
    return {
        "name": sample.name,
        "value": sample.value,
        "labels": sample.labels,
        "recorded_at": sample.recorded_at.isoformat(),
    }


@app.post("/traces")
def record_trace(request: TraceRequest) -> Dict[str, str]:
    attributes = request.attributes or {}
    span = hub.record_trace(request.span_id, request.name, request.duration_ms, **attributes)
    return {
        "span_id": span.span_id,
        "name": span.name,
        "duration_ms": span.duration_ms,
        "attributes": span.attributes,
        "timestamp": span.timestamp.isoformat(),
    }


@app.post("/logs")
def record_log(request: LogRequest) -> Dict[str, str]:
    context = request.context or {}
    log = hub.record_log(request.level, request.message, **context)
    return {
        "level": log.level,
        "message": log.message,
        "timestamp": log.timestamp.isoformat(),
        "context": log.context,
    }


@app.get("/snapshot")
def snapshot() -> Dict[str, object]:
    return hub.snapshot()


@app.get("/metrics/prometheus")
def prometheus() -> str:
    return hub.render_prometheus()
