# 07 — Integração no cartorio-digital (APIs & Jobs)

## Propostas de endpoints
- `POST /api/internal/sign/pades`: recebe `file_id`/`pdf_url`, aplica PAdES + timestamp e retorna `signed_url`.
- `POST /api/internal/sign/jar`: recebe `artifact_url`, assina JAR e retorna artefato assinado.
- `POST /api/internal/timestamp`: recebe `hash`/`file_url`, retorna `tsr` e metadados.
- `GET /api/public/verificacao/{hash}`: exibe status e evidências (conecta com verificação pública do Cap. 6/7).

## Jobs/Filas
- Orquestrar assinatura e timestamp como **tarefas** assíncronas com **reprocessamento** idempotente.
- Persistir **metadados** das assinaturas e **TSR** no storage do projeto.