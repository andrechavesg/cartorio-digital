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

### Provisionamento com Terraform
```hcl
resource "aws_kms_key" "assinatura_cartorio" {
  description             = "Chave KMS para assinatura do Cartório Digital"
  key_usage               = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  deletion_window_in_days = 30
}

resource "aws_kms_alias" "assinatura_cartorio" {
  name          = "alias/cartorio-assinatura"
  target_key_id = aws_kms_key.assinatura_cartorio.key_id
}
```
As chaves assimétricas de assinatura (`KeyUsage = "SIGN_VERIFY"`) com `customer_master_key_spec = "RSA_2048"` **não** suportam rotação automática; por isso, a opção `enable_key_rotation` não aparece no recurso acima.

> **Planejamento de rotação manual:** para renovar chaves de assinatura, crie uma nova CMK com as mesmas características, atualize a aplicação/alias para apontar para a nova chave, valide as novas assinaturas e, somente após a transição completa, desative e agende a exclusão da chave antiga. Registre o processo e mantenha evidências de teste para auditoria.
