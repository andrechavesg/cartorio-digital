# Módulo 7 &mdash; Assinatura de Artefatos

Com o KMS integrado, adicionamos certificados de code signing e um fluxo de assinatura de artefatos para distribuir software com cadeias confiáveis.

## Destaques

- AC dedicada para emissão de certificados com EKU `CODE_SIGNING`.
- Alias gerenciados pelo KMS para proteger as chaves privadas.
- Registro de artefatos assinados contendo hash (SHA-256) e assinatura base64.

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

1. Gere uma credencial de code signing.
2. Assine um artefato informando alias e payload (ex.: conteúdo de um binário ou manifesto).
3. Consulte `/signing/artifacts` para visualizar hash, assinatura e metadados.
4. Use a chave pública do certificado emitido para validar a assinatura externamente.

Essa etapa prepara a automação de pipelines e liberações seguras abordada no próximo módulo.
