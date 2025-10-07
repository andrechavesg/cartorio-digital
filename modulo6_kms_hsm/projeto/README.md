# Módulo 6 &mdash; Integração com KMS/HSM

Nesta etapa as chaves privadas de titulares não ficam mais em disco. Uma camada mock de KMS/HSM gera, armazena e usa as chaves para assinar dados, enquanto a AC emite o certificado com base na chave protegida.

## Destaques

- `MockKMS` expõe operações de criação de chave e assinatura (PSS/SHA-256).
- AC gera certificados usando apenas a chave pública retornada pelo KMS (suportando key alias).
- Endpoint para assinatura de payloads, simulando uso de HSM para operações sensíveis.

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

1. Emita uma credencial via `/credentials` e observe o alias retornado.
2. Liste as chaves em `/kms/keys` para confirmar que o alias está protegido pelo KMS.
3. Utilize `/kms/sign` informando `alias` e `payload` para gerar uma assinatura base64.
4. Repare que o certificado emitido contém a chave pública proveniente do KMS.

A partir daqui podemos assinar artefatos e integrar pipelines usando as chaves protegidas.
