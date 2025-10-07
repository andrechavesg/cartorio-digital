# Módulo 9 &mdash; Observabilidade

Implementamos um hub de observabilidade que coleta métricas, traces e logs das operações do cartório digital, permitindo inspecionar o comportamento dos módulos anteriores.

## Destaques

- Registro de métricas com labels e exportação em formato compatível com Prometheus.
- Coleta de traces e logs em memória para rápida investigação.
- API `/snapshot` que agrega todo o estado observável para dashboards.

## Execução

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

Frontend:

```bash
cd ../frontend
python -m http.server 3000
```

## Validação

1. Registre métricas, traces e logs via frontend ou cURL.
2. Consulte `/snapshot` para ver o estado consolidado.
3. Acesse `/metrics/prometheus` e valide que o formato é aceito por Prometheus.
4. Integre as chamadas com pipelines do módulo 8 para simular monitoramento contínuo.

Esse módulo finaliza a base necessária para o projeto completo do módulo 10.
