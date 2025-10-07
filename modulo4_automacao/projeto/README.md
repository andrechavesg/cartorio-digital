# Módulo 4 &mdash; Automação ACME

Este módulo automatiza a emissão de certificados de servidor usando um fluxo inspirado no protocolo ACME (Let's Encrypt), com ordens, desafios e validação.

## Destaques

- Geração de tokens de desafio para comprovação de domínio.
- Emissão automática de certificados de servidor após a validação do token.
- Listagem de ordens ACME para acompanhamento do fluxo.

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

1. Crie uma nova ordem ACME informando o domínio.
2. Copie o `token` retornado e simule a validação do desafio com o mesmo token na rota `/acme/orders/{id}/complete`.
3. Ao completar o desafio o backend emitirá um certificado de servidor e marcará a ordem como `valid`.
4. Consulte `/acme/orders` para auditar o status e número de série do certificado.

Nos próximos módulos incorporamos requisitos regulatórios e integrações corporativas ao fluxo.
