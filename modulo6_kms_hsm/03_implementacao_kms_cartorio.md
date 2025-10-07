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

  tags = {
    Sistema  = "CartorioDigital"
    Ambiente = var.ambiente
    Uso      = "Assinatura"
  }
}
```

> Replique o recurso para chaves de criptografia (`key_usage = "ENCRYPT_DECRYPT"`) e para selos de tempo.

## 3. Fluxo de assinatura com boto3 (Python)

Antes de mergulhar no código, é importante reconhecer que a aplicação precisava de um mecanismo comprovadamente auditável para cada assinatura emitida, garantindo validade jurídica sem abrir mão da agilidade. A integração com o **AWS KMS** responde a esse desafio ao unir trilhas de auditoria nativas com a governança centralizada do Cartório Digital, inspirando a equipe a buscar sempre o equilíbrio entre segurança e inovação.

### Fluxo de assinatura com boto3 (Python)
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
### Integração prática
Cada **documento assinado** usa uma **data key** derivada de uma CMK (envelope encryption)
para garantir segregação e rastreabilidade.

Ao expandir a operação para múltiplas contas e ambientes do Cartório Digital, surgia a dor recorrente de manter um padrão consistente de **Customer Master Keys (CMKs)** sem depender de etapas manuais suscetíveis a erro. Adotar o Terraform como linguagem comum de provisionamento trouxe a garantia de que cada conta herda exatamente a mesma configuração de segurança, fortalecendo a governança compartilhada entre os cartórios.

### Provisionamento via IaC (Terraform)
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
O snippet evidencia como o projeto principal preserva a padronização ao encapsular a criação da CMK e do alias em código versionado, assegurando que cada cartório virtual receba a mesma estrutura de chaves que sustenta os fluxos de assinatura descritos acima.
As chaves assimétricas de assinatura (`KeyUsage = "SIGN_VERIFY"`) com `customer_master_key_spec = "RSA_2048"` **não** suportam rotação automática porque o KMS não gera automaticamente novos pares de chaves RSA assinadoras: a rotação exige a criação de um novo par criptográfico e migração planejada do alias para preservar a rastreabilidade das assinaturas jurídicas.

### Processo de rotação manual para o Cartório Digital

1. **Provisione uma nova CMK** com os mesmos parâmetros (`KeyUsage = "SIGN_VERIFY"`, `customer_master_key_spec = "RSA_2048"`) em uma janela de mudança aprovada.
2. **Troque o alias** (`aws_kms_alias`) e políticas aplicáveis para apontarem para a nova CMK, validando a compatibilidade das aplicações e a capacidade de verificação das assinaturas emitidas.
3. **Planeje a desativação da CMK antiga**, mantendo-a ativa apenas pelo período de convivência acordado; em seguida, desative-a e agende a exclusão quando não houver mais dependências. Documente cada etapa, as evidências de teste e as aprovações de governança para fins de auditoria.
