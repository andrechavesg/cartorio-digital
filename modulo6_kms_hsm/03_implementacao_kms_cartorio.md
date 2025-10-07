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
  description              = "Chave KMS para assinatura do Cartório Digital"
  key_usage                = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  deletion_window_in_days  = 30
}

resource "aws_kms_alias" "assinatura_cartorio" {
  name          = "alias/cartorio-assinatura"
  target_key_id = aws_kms_key.assinatura_cartorio.key_id
}
```

CMKs usadas para **assinatura** (`KeyUsage = "SIGN_VERIFY"`) com `customer_master_key_spec = "RSA_2048"`
não suportam rotação automática pelo KMS. Para o Cartório Digital, isso significa que a rotação deve ser
planejada e executada manualmente, obedecendo às janelas de mudança e aos controles de conformidade
estabelecidos para os selos digitais.

> **Nota sobre rotação manual planejada:** crie uma nova CMK com a mesma configuração, atualize aliases
> e políticas para apontar para a nova chave, valide a aplicação e os fluxos de assinatura, e somente após
> a transição controlada desative e agende a exclusão da CMK anterior. Registre os passos, evidências de
> teste e aprovações para sustentar auditorias futuras.
