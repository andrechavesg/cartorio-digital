# Módulo 1 &mdash; Fundamentos

Este módulo estabelece a base do Cartório Digital com um backend FastAPI e uma página HTML simples. O objetivo é permitir o cadastro de cidadãos que serão usados nos próximos módulos.

## Estrutura

- `backend/`: API em FastAPI com rotas de saúde e cadastro/listagem de cidadãos.
- `frontend/`: página estática que consome a API.

## Execução

1. Crie um ambiente virtual e instale as dependências:

   ```bash
   cd backend
   python -m venv .venv
   source .venv/bin/activate
   pip install -r requirements.txt
   ```

2. Inicie a API:

   ```bash
   uvicorn main:app --reload
   ```

3. Em outro terminal, sirva o frontend (opcionalmente com o Python HTTP server):

   ```bash
   cd ../frontend
   python -m http.server 3000
   ```

4. Acesse `http://localhost:3000` e cadastre cidadãos. A API responde em `http://localhost:8000`.

## Validação

- Chame `GET http://localhost:8000/health` e confirme que responde `{ "status": "ok" }`.
- Cadastre cidadãos via frontend ou `POST http://localhost:8000/citizens` com JSON `{ "name": "Alice", "email": "alice@example.com" }`.
- Liste cidadãos em `GET http://localhost:8000/citizens`.

Nos próximos módulos os registros serão usados para emissão de certificados.
