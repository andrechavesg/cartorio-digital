# Módulo 5 &mdash; Requisitos Regulatórios

Neste estágio adicionamos controles de compliance exigidos por normativos (como ITI e ICP-Brasil), garantindo que certificados só sejam emitidos quando a documentação estiver adequada.

## Destaques

- Cadastro de cidadão com tipo e número de documento.
- Motor de compliance com regras simples (tipo de documento permitido, tamanho mínimo do número e validade mínima do certificado).
- Auditoria de emissões com resultados de cada regra.

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

1. Cadastre cidadãos com documentos válidos.
2. Solicite um certificado via `/certificates/compliant`.
3. Verifique o retorno das regras de compliance e o certificado emitido.
4. Tente informar um tipo de documento inválido para ver a emissão sendo bloqueada com detalhes no log.

Estes controles serão necessários quando integrarmos KMS/HSM e processos auditáveis nos módulos seguintes.
