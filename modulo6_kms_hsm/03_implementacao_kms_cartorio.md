# Implementação de KMS no Cartório Digital

Integração do **AWS KMS** à aplicação para proteger chaves usadas na emissão/validação de certificados.

### Exemplo de uso com boto3 (Python)
```python
import boto3

kms = boto3.client('kms')

# Criação de chave mestra
response = kms.create_key(
    Description='Chave mestra do Cartório Digital',
    KeyUsage='SIGN_VERIFY',
    Origin='AWS_KMS'
)
key_id = response['KeyMetadata']['KeyId']

# Uso para assinar documento
message = b"Documento oficial do Cartório Digital"
sign_response = kms.sign(
    KeyId=key_id,
    Message=message,
    SigningAlgorithm='RSASSA_PSS_SHA_256'
)
signature = sign_response['Signature']
```

### Integração prática
Cada **documento assinado** usa uma **data key** derivada de uma CMK (envelope encryption)
para garantir segregação e rastreabilidade.
