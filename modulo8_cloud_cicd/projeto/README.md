# Módulo 8 &mdash; Cloud e CI/CD

Conectamos a emissão de certificados e assinatura de artefatos a um pipeline CI/CD simulando deploys em nuvem, demonstrando como integrar governança de certificados aos processos de entrega contínua.

## Destaques

- Orquestrador de pipeline que gera certificados de code signing, assina artefatos e marca deploys por ambiente.
- Persistência em memória dos pipelines executados, com passos e timestamps.
- Exposição de chaves gerenciadas pelo KMS para auditoria de infraestrutura.

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

1. Execute um pipeline informando aplicação, ambiente e payload.
2. Verifique as etapas (`build`, `sign`, `deploy`) e os detalhes no retorno.
3. Consulte `/pipelines` e `/kms/keys` para acompanhar execuções e chaves criadas automaticamente.
4. Observe que cada execução cria uma nova credencial com alias exclusivo e assinatura vinculada.

O próximo módulo adiciona observabilidade para monitorar todo esse ecossistema.
