# Módulo 2 &mdash; PKI e Certificados

Avançamos para a criação de uma autoridade certificadora (AC) interna capaz de emitir certificados X.509 para os cidadãos cadastrados no módulo anterior.

## Destaques

- Autoridade certificadora raiz gerada em memória com chave RSA 2048 bits.
- Rotas para emissão, listagem, detalhamento e revogação de certificados.
- Frontend atualizado para exibir o certificado da AC e os certificados emitidos.

## Execução

1. Configure o backend:

   ```bash
   cd backend
   python -m venv .venv
   source .venv/bin/activate
   pip install -r requirements.txt
   uvicorn main:app --reload
   ```

2. Sirva o frontend estático:

   ```bash
   cd ../frontend
   python -m http.server 3000
   ```

3. Navegue para `http://localhost:3000`.

## Validação

- Cadastre cidadãos via frontend.
- Emita certificados informando `Common Name` e e-mail cadastrado.
- Clique em "Exibir certificado raiz" para obter o PEM da AC.
- Liste certificados e teste a revogação com `POST /certificates/{serial}/revoke` (via `curl` ou ferramenta HTTP).

Este módulo estabelece a base de PKI usada nos próximos capítulos para TLS, ACME e governança.
