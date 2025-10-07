# Módulo 3 &mdash; TLS e mTLS

Este módulo expande a AC para suportar certificados de servidor e cliente, além de uma simulação de handshake mTLS utilizando as emissões locais.

## Destaques

- Emissão diferenciada de certificados para cliente (`ExtendedKeyUsage=CLIENT_AUTH`) e servidor (`SERVER_AUTH`).
- Função `validate_mutual_tls` que verifica revogação e validade antes de aceitar o handshake.
- Frontend com simulação de validação mTLS.

## Execução

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

Sirva o frontend em outro terminal:

```bash
cd ../frontend
python -m http.server 3000
```

## Validação

1. Cadastre um cidadão.
2. Emita um certificado de cliente para o e-mail cadastrado.
3. Emita um certificado de servidor com o hostname desejado.
4. Use os números de série retornados na rota `/tls/handshake` para validar a sessão.
5. Opcionalmente revogue certificados editando a lógica no backend para observar falhas no handshake.

Esses conceitos são utilizados nos módulos seguintes para TLS com ACME e automação.
