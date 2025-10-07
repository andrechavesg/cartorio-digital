"""In-memory observability toolkit for the lab."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Dict, List


@dataclass
class MetricSample:
    name: str
    value: float
    labels: Dict[str, str]
    recorded_at: datetime


@dataclass
class TraceSpan:
    span_id: str
    name: str
    duration_ms: float
    attributes: Dict[str, str]
    timestamp: datetime


@dataclass
class LogEntry:
    level: str
    message: str
    timestamp: datetime
    context: Dict[str, str]


class ObservabilityHub:
    def __init__(self) -> None:
        self.metrics: List[MetricSample] = []
        self.traces: List[TraceSpan] = []
        self.logs: List[LogEntry] = []

    def record_metric(self, name: str, value: float, **labels: str) -> MetricSample:
        sample = MetricSample(name=name, value=value, labels=labels, recorded_at=datetime.now(timezone.utc))
        self.metrics.append(sample)
        return sample

    def record_trace(self, span_id: str, name: str, duration_ms: float, **attributes: str) -> TraceSpan:
        span = TraceSpan(
            span_id=span_id,
            name=name,
            duration_ms=duration_ms,
            attributes=attributes,
            timestamp=datetime.now(timezone.utc),
        )
        self.traces.append(span)
        return span

    def record_log(self, level: str, message: str, **context: str) -> LogEntry:
        log = LogEntry(
            level=level,
            message=message,
            timestamp=datetime.now(timezone.utc),
            context=context,
        )
        self.logs.append(log)
        return log

    def snapshot(self) -> Dict[str, List[Dict[str, str]]]:
        return {
            "metrics": [
                {
                    "name": sample.name,
                    "value": sample.value,
                    "labels": sample.labels,
                    "recorded_at": sample.recorded_at.isoformat(),
                }
                for sample in self.metrics
            ],
            "traces": [
                {
                    "span_id": span.span_id,
                    "name": span.name,
                    "duration_ms": span.duration_ms,
                    "attributes": span.attributes,
                    "timestamp": span.timestamp.isoformat(),
                }
                for span in self.traces
            ],
            "logs": [
                {
                    "level": log.level,
                    "message": log.message,
                    "timestamp": log.timestamp.isoformat(),
                    "context": log.context,
                }
                for log in self.logs
            ],
        }

    def render_prometheus(self) -> str:
        lines = []
        for sample in self.metrics:
            label_parts = ",".join(f"{key}='{value}'" for key, value in sample.labels.items())
            label_block = f"{{{label_parts}}}" if label_parts else ""
            lines.append(f"{sample.name}{label_block} {sample.value}")
        return "\n".join(lines)
