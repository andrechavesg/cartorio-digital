# Implementação de KMS no Cartório Digital

Nesta etapa configuramos o AWS KMS para custodiar as chaves usadas na emissão de certificados, assinatura de documentos e criptografia de dados sensíveis do cartório.

## 1. Planejamento

1. **Mapeie os casos de uso**: assinatura de certidões, criptografia de dados pessoais, selos de tempo.
2. **Defina regiões e contas**: separar ambientes (dev, homolog, prod) em contas distintas com CMKs próprias.
3. **Elabore política de nomes/tags**: use tags `Sistema=CartorioDigital`, `Ambiente=Prod`, `Uso=Assinatura` para facilitar auditoria.

## 2. Provisionamento via IaC

Exemplo simplificado usando **Terraform**:

```hcl
resource "aws_kms_key" "assinatura_cartorio" {
  description             = "CMK para assinatura de certidoes digitais"
  deletion_window_in_days = 30
  key_usage               = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  enable_key_rotation     = true

  tags = {
    Sistema  = "CartorioDigital"
    Ambiente = var.ambiente
    Uso      = "Assinatura"
  }
}
```

> Replique o recurso para chaves de criptografia (`key_usage = "ENCRYPT_DECRYPT"`) e para selos de tempo.

## 3. Fluxo de assinatura com boto3 (Python)

```python
import boto3

kms = boto3.client("kms", region_name="sa-east-1")
KEY_ID = "arn:aws:kms:sa-east-1:123456789012:key/uuid-da-cmk"

def assinar_documento(payload: bytes) -> bytes:
    response = kms.sign(
        KeyId=KEY_ID,
        Message=payload,
        MessageType="RAW",
        SigningAlgorithm="RSASSA_PSS_SHA_256",
    )
    return response["Signature"]

def verificar_assinatura(payload: bytes, assinatura: bytes) -> bool:
    response = kms.verify(
        KeyId=KEY_ID,
        Message=payload,
        Signature=assinatura,
        MessageType="RAW",
        SigningAlgorithm="RSASSA_PSS_SHA_256",
    )
    return response["SignatureValid"]
```

### Boas práticas

- **Grants temporários:** use `create_grant` para permitir que jobs assinem documentos por tempo limitado.
- **Envelope encryption:** para dados volumosos (por ex. anexos), gere data keys com `generate_data_key` e armazene apenas o blob criptografado.
- **Secrets externos:** nunca faça download da chave privada; toda operação ocorre dentro do serviço KMS.

## 4. Observabilidade

- Ative **CloudTrail** para registrar `Sign`, `Decrypt`, `GenerateDataKey`.
- Configure métricas no **CloudWatch** (invocações, erros, throttling).
- Envie logs críticos para o SIEM corporativo.
