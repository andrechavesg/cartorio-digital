# 08 — Testes Automatizados

## Escopo mínimo
- **Unidade**: controle de entrada (formatos, MIME), cálculo de hash, montagem de TSQ.
- **Integração**: chamadas à TSA (mock), validação de retorno, persistência de evidências.
- **End-to-end**: fluxo PDF -> PAdES -> timestamp -> validação -> relatório.

## Dicas
- Mocks para TSA (respostas determinísticas `.tsr`).
- Snapshot tests para payloads de API (OpenAPI ajuda a estabilizar contratos).