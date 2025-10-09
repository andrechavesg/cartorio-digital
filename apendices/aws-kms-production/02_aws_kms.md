# Capítulo 2 – AWS KMS para Proteção de Chaves

## O que é AWS KMS

O **AWS Key Management Service (KMS)** é um serviço gerenciado que facilita a criação e o controle de chaves de criptografia (Customer Master Keys - CMKs) usadas para criptografar seus dados.

### Por que KMS para PKI?

No contexto de PKI, o KMS protege:

1. **Chaves privadas de CA** (Root e Intermediate)
2. **Chaves de encryption at-rest** (RDS, S3)
3. **Chaves de assinatura** de tokens JWT
4. **Chaves de encryption** do Secrets Manager

### Vantagens do KMS

✅ **Segurança:** Chaves nunca saem do HSM  
✅ **Auditoria:** CloudTrail registra todos os usos  
✅ **Conformidade:** FIPS 140-2 Level 2/3  
✅ **Integração:** Nativo com serviços AWS  
✅ **Rotação:** Automática (opcional)  
✅ **Custo:** $1/chave/mês + $0.03/10k requests  

## Tipos de chaves no KMS

### 1. AWS Managed Keys

- **Gerenciadas automaticamente** pela AWS
- Rotação automática a cada 3 anos
- Não podem ser deletadas
- Gratuitas

**Exemplo:** `aws/rds`, `aws/s3`, `aws/secretsmanager`

### 2. Customer Managed Keys (CMKs)

- **Você controla** políticas e lifecycle
- Rotação opcional (anual)
- Podem ser agendadas para deleção
- **$1/mês por chave**

**Uso recomendado para PKI**

### 3. AWS Owned Keys

- Gerenciadas pela AWS
- Invisíveis para você
- Usadas internamente por alguns serviços

## Arquitetura de chaves do Cartório Digital

```
┌──────────────────────────────────────────────────────────┐
│              AWS KMS Key Hierarchy                       │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  cartorio-root-ca-key (RSA 4096)                   │ │
│  │                                                    │ │
│  │  Purpose: Proteger chave privada da Root CA       │ │
│  │  Key Store: CloudHSM (FIPS 140-2 Level 3)         │ │
│  │  Rotation: Manual (apenas para emergências)       │ │
│  │  Access: Apenas IAM role "CA-Administrator"       │ │
│  │                                                    │ │
│  │  KMS Key Policy:                                  │ │
│  │  • kms:CreateGrant → CA-Administrator            │ │
│  │  • kms:Decrypt → Private-CA-Service              │ │
│  │  • kms:DescribeKey → Auditors                    │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                            │                             │
│                            │ signs                       │
│                            ▼                             │
│  ┌────────────────────────────────────────────────────┐ │
│  │  cartorio-intermediate-tls-ca-key (RSA 2048)       │ │
│  │                                                    │ │
│  │  Purpose: Intermediate CA para certificados TLS   │ │
│  │  Key Store: KMS Standard (FIPS 140-2 Level 2)     │ │
│  │  Rotation: Automatic (annual)                     │ │
│  │  Access: Private-CA-Service, Backend-API          │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  cartorio-intermediate-doc-ca-key (RSA 2048)       │ │
│  │                                                    │ │
│  │  Purpose: Intermediate CA para assinatura docs    │ │
│  │  Key Store: KMS Standard                          │ │
│  │  Rotation: Automatic (annual)                     │ │
│  │  Access: Private-CA-Service, Backend-API          │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  cartorio-data-encryption-key (AES 256)            │ │
│  │                                                    │ │
│  │  Purpose: Encryption at-rest (RDS, S3, Secrets)   │ │
│  │  Key Store: KMS Standard                          │ │
│  │  Rotation: Automatic (enabled)                    │ │
│  │  Access: RDS, S3, Secrets Manager services        │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## Criando CMKs para o Cartório Digital

### Chave para Root CA (com CloudHSM)

```bash
#!/bin/bash
# create-root-ca-key.sh

# 1. Criar CMK associada ao CloudHSM custom key store
aws kms create-key \
  --description "Cartorio Digital Root CA Private Key" \
  --key-usage SIGN_VERIFY \
  --customer-master-key-spec RSA_4096 \
  --origin AWS_CLOUDHSM \
  --custom-key-store-id cks-1234567890abcdef \
  --tags TagKey=Project,TagValue=CartorioDigital \
         TagKey=Environment,TagValue=production \
         TagKey=Purpose,TagValue=RootCA \
  --region sa-east-1

# Capturar Key ID
ROOT_CA_KEY_ID=$(aws kms list-keys --query 'Keys[?KeyId==`arn:aws:kms:sa-east-1:123456789012:key/12345678-1234-1234-1234-123456789012`].KeyId' --output text)

# 2. Criar alias
aws kms create-alias \
  --alias-name alias/cartorio-root-ca-key \
  --target-key-id $ROOT_CA_KEY_ID

# 3. Aplicar Key Policy restritiva
cat > root-ca-key-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Id": "root-ca-key-policy",
  "Statement": [
    {
      "Sid": "Enable IAM User Permissions",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:root"
      },
      "Action": "kms:*",
      "Resource": "*"
    },
    {
      "Sid": "Allow CA Administrator",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:role/CA-Administrator"
      },
      "Action": [
        "kms:CreateGrant",
        "kms:DescribeKey",
        "kms:GetPublicKey"
      ],
      "Resource": "*"
    },
    {
      "Sid": "Allow Private CA Service",
      "Effect": "Allow",
      "Principal": {
        "Service": "acm-pca.amazonaws.com"
      },
      "Action": [
        "kms:Decrypt",
        "kms:Sign",
        "kms:GetPublicKey"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "kms:ViaService": "acm-pca.sa-east-1.amazonaws.com"
        }
      }
    },
    {
      "Sid": "Allow Auditors Read-Only",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:role/SecurityAuditor"
      },
      "Action": [
        "kms:DescribeKey",
        "kms:GetKeyPolicy",
        "kms:GetKeyRotationStatus",
        "kms:ListGrants",
        "kms:ListKeyPolicies"
      ],
      "Resource": "*"
    }
  ]
}
EOF

aws kms put-key-policy \
  --key-id $ROOT_CA_KEY_ID \
  --policy-name default \
  --policy file://root-ca-key-policy.json

echo "✅ Root CA Key created: $ROOT_CA_KEY_ID"
```

### Chave para Intermediate CA (Standard KMS)

```bash
#!/bin/bash
# create-intermediate-ca-keys.sh

# Função para criar Intermediate CA key
create_intermediate_key() {
  local PURPOSE=$1
  local DESCRIPTION=$2
  
  KEY_ID=$(aws kms create-key \
    --description "$DESCRIPTION" \
    --key-usage SIGN_VERIFY \
    --customer-master-key-spec RSA_2048 \
    --origin AWS_KMS \
    --tags TagKey=Project,TagValue=CartorioDigital \
           TagKey=Environment,TagValue=production \
           TagKey=Purpose,TagValue=$PURPOSE \
    --region sa-east-1 \
    --query 'KeyMetadata.KeyId' \
    --output text)
  
  aws kms create-alias \
    --alias-name alias/cartorio-intermediate-$PURPOSE-ca-key \
    --target-key-id $KEY_ID
  
  # Habilitar rotação automática
  aws kms enable-key-rotation --key-id $KEY_ID
  
  echo "✅ Intermediate CA Key ($PURPOSE) created: $KEY_ID"
}

# Criar chaves para cada Intermediate CA
create_intermediate_key "tls" "Cartorio Digital Intermediate CA for TLS Certificates"
create_intermediate_key "document" "Cartorio Digital Intermediate CA for Document Signing"
```

### Chave para Data Encryption

```bash
#!/bin/bash
# create-data-encryption-key.sh

DATA_KEY_ID=$(aws kms create-key \
  --description "Cartorio Digital Data Encryption Key (RDS, S3, Secrets Manager)" \
  --key-usage ENCRYPT_DECRYPT \
  --customer-master-key-spec SYMMETRIC_DEFAULT \
  --origin AWS_KMS \
  --tags TagKey=Project,TagValue=CartorioDigital \
         TagKey=Environment,TagValue=production \
         TagKey=Purpose,TagValue=DataEncryption \
  --region sa-east-1 \
  --query 'KeyMetadata.KeyId' \
  --output text)

aws kms create-alias \
  --alias-name alias/cartorio-data-encryption-key \
  --target-key-id $DATA_KEY_ID

# Habilitar rotação automática
aws kms enable-key-rotation --key-id $DATA_KEY_ID

# Aplicar Key Policy
cat > data-encryption-key-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Enable IAM User Permissions",
      "Effect": "Allow",
      "Principal": {"AWS": "arn:aws:iam::123456789012:root"},
      "Action": "kms:*",
      "Resource": "*"
    },
    {
      "Sid": "Allow RDS",
      "Effect": "Allow",
      "Principal": {"Service": "rds.amazonaws.com"},
      "Action": ["kms:Decrypt", "kms:GenerateDataKey", "kms:CreateGrant"],
      "Resource": "*"
    },
    {
      "Sid": "Allow S3",
      "Effect": "Allow",
      "Principal": {"Service": "s3.amazonaws.com"},
      "Action": ["kms:Decrypt", "kms:GenerateDataKey"],
      "Resource": "*"
    },
    {
      "Sid": "Allow Secrets Manager",
      "Effect": "Allow",
      "Principal": {"Service": "secretsmanager.amazonaws.com"},
      "Action": ["kms:Decrypt", "kms:GenerateDataKey", "kms:CreateGrant"],
      "Resource": "*"
    }
  ]
}
EOF

aws kms put-key-policy \
  --key-id $DATA_KEY_ID \
  --policy-name default \
  --policy file://data-encryption-key-policy.json

echo "✅ Data Encryption Key created: $DATA_KEY_ID"
```

## Usando KMS com AWS Private CA

### Criar Root CA com KMS-protected key

```bash
#!/bin/bash
# create-private-ca-with-kms.sh

ROOT_CA_KEY_ARN="arn:aws:kms:sa-east-1:123456789012:key/12345678-1234-1234-1234-123456789012"

# Criar Root CA
ROOT_CA_ARN=$(aws acm-pca create-certificate-authority \
  --certificate-authority-configuration file://root-ca-config.json \
  --certificate-authority-type ROOT \
  --revocation-configuration file://revocation-config.json \
  --key-storage-security-standard FIPS_140_2_LEVEL_3_OR_HIGHER \
  --tags Key=Name,Value=CartorioDigitalRootCA \
  --region sa-east-1 \
  --query 'CertificateAuthorityArn' \
  --output text)

# Configuração da CA
cat > root-ca-config.json <<EOF
{
  "KeyAlgorithm": "RSA_4096",
  "SigningAlgorithm": "SHA512WITHRSA",
  "Subject": {
    "Country": "BR",
    "Organization": "Cartorio Digital",
    "OrganizationalUnit": "PKI",
    "CommonName": "Cartorio Digital Root CA"
  }
}
EOF

# Configuração de revogação
cat > revocation-config.json <<EOF
{
  "CrlConfiguration": {
    "Enabled": true,
    "ExpirationInDays": 7,
    "CustomCname": "crl.cartorio.gov.br",
    "S3BucketName": "cartorio-prod-crl"
  },
  "OcspConfiguration": {
    "Enabled": true
  }
}
EOF

echo "✅ Root CA created: $ROOT_CA_ARN"
echo "Now you need to issue the root certificate (see AWS Private CA docs)"
```

## Auditoria de uso de chaves

### CloudTrail events para KMS

Todos os usos de chaves KMS são registrados no CloudTrail:

```json
{
  "eventVersion": "1.08",
  "userIdentity": {
    "type": "AssumedRole",
    "principalId": "AIDACKCEVSQ6C2EXAMPLE",
    "arn": "arn:aws:sts::123456789012:assumed-role/Private-CA-Service/session",
    "accountId": "123456789012",
    "sessionContext": {
      "sessionIssuer": {
        "type": "Role",
        "principalId": "AIDACKCEVSQ6C2EXAMPLE",
        "arn": "arn:aws:iam::123456789012:role/Private-CA-Service"
      }
    }
  },
  "eventTime": "2025-10-09T14:30:00Z",
  "eventSource": "kms.amazonaws.com",
  "eventName": "Decrypt",
  "awsRegion": "sa-east-1",
  "sourceIPAddress": "10.0.11.45",
  "requestParameters": {
    "keyId": "arn:aws:kms:sa-east-1:123456789012:key/12345678-1234-1234-1234-123456789012",
    "encryptionContext": {
      "aws:acm-pca:arn": "arn:aws:acm-pca:sa-east-1:123456789012:certificate-authority/abcd1234"
    }
  },
  "responseElements": null,
  "requestID": "ff000000-ff00-ff00-ff00-ffffffffffff",
  "eventID": "00000000-0000-0000-0000-000000000000",
  "readOnly": true,
  "resources": [
    {
      "accountId": "123456789012",
      "type": "AWS::KMS::Key",
      "ARN": "arn:aws:kms:sa-east-1:123456789012:key/12345678-1234-1234-1234-123456789012"
    }
  ],
  "eventType": "AwsApiCall",
  "managementEvent": true
}
```

### Alertas de uso suspeito

```python
# lambda_function.py
# Lambda que monitora uso suspeito de chaves KMS

import boto3
import json
from datetime import datetime, timedelta

sns = boto3.client('sns')
cloudwatch = boto3.client('cloudwatch')

def lambda_handler(event, context):
    """
    Triggered por CloudWatch Events para KMS operations.
    Alerta em caso de uso suspeito.
    """
    
    detail = event['detail']
    event_name = detail['eventName']
    user_arn = detail['userIdentity']['arn']
    key_id = detail['requestParameters']['keyId']
    source_ip = detail['sourceIPAddress']
    
    # Regras de detecção
    suspicious = False
    reason = ""
    
    # Regra 1: Decrypt de Root CA key (deve ser raro)
    if 'root-ca-key' in key_id and event_name == 'Decrypt':
        suspicious = True
        reason = "Root CA key decrypt detected"
    
    # Regra 2: IP fora da VPC
    if not source_ip.startswith('10.0.'):
        suspicious = True
        reason = f"KMS operation from external IP: {source_ip}"
    
    # Regra 3: Horário fora do expediente
    hour = datetime.now().hour
    if hour < 6 or hour > 22:
        suspicious = True
        reason = "KMS operation outside business hours"
    
    if suspicious:
        message = {
            "alert": "Suspicious KMS Usage Detected",
            "reason": reason,
            "event": event_name,
            "user": user_arn,
            "key": key_id,
            "source_ip": source_ip,
            "timestamp": detail['eventTime']
        }
        
        # Enviar alerta SNS
        sns.publish(
            TopicArn='arn:aws:sns:sa-east-1:123456789012:security-alerts',
            Subject=f'🚨 Suspicious KMS Usage: {reason}',
            Message=json.dumps(message, indent=2)
        )
        
        # Registrar métrica no CloudWatch
        cloudwatch.put_metric_data(
            Namespace='CartorioDigital/Security',
            MetricData=[{
                'MetricName': 'SuspiciousKMSUsage',
                'Value': 1,
                'Unit': 'Count',
                'Timestamp': datetime.now()
            }]
        )
    
    return {'statusCode': 200}
```

## Rotação de chaves

### Rotação automática (KMS nativo)

```bash
# Habilitar rotação automática (anual)
aws kms enable-key-rotation --key-id alias/cartorio-intermediate-tls-ca-key

# Verificar status de rotação
aws kms get-key-rotation-status --key-id alias/cartorio-intermediate-tls-ca-key
```

**Como funciona:**
- AWS cria nova versão da chave automaticamente
- Chave antiga permanece disponível para decrypt de dados antigos
- Encrypt usa sempre a versão mais recente
- Transparente para aplicações

### Rotação manual (para Root CA)

```bash
#!/bin/bash
# rotate-root-ca-key-manual.sh

# ATENÇÃO: Este processo requer MUITA coordenação
# - Requer reemissão de toda a cadeia de certificados
# - Deve ser feito apenas em emergências (key compromise)

# 1. Criar nova chave
NEW_ROOT_KEY_ID=$(aws kms create-key \
  --description "Cartorio Digital Root CA Private Key (v2)" \
  --key-usage SIGN_VERIFY \
  --customer-master-key-spec RSA_4096 \
  --origin AWS_CLOUDHSM \
  --query 'KeyMetadata.KeyId' \
  --output text)

# 2. Criar nova Root CA com nova chave
NEW_ROOT_CA_ARN=$(aws acm-pca create-certificate-authority \
  --certificate-authority-configuration file://root-ca-config-v2.json \
  --certificate-authority-type ROOT \
  --query 'CertificateAuthorityArn' \
  --output text)

# 3. Emitir certificado da nova Root CA
# (processo detalhado no capítulo 3)

# 4. Emitir novos certificados de Intermediate CAs assinados pela nova Root

# 5. Gradualmente revogar certificados da antiga Root CA

# 6. Após período de transição, desabilitar antiga Root CA
aws acm-pca update-certificate-authority \
  --certificate-authority-arn $OLD_ROOT_CA_ARN \
  --status DISABLED

echo "⚠️  Root CA key rotation completed. Monitor systems for issues."
```

## Custos de KMS

### Pricing (região sa-east-1)

| Item | Preço |
|------|-------|
| **CMK (Customer Master Key)** | $1.00/mês |
| **API Requests** | $0.03 per 10.000 requests |
| **CloudHSM Custom Key Store** | $1.45/hora por HSM cluster |

### Estimativa para Cartório Digital

```
3 CMKs (Root + 2 Intermediates):        $3.00/mês
1 Data Encryption CMK:                    $1.00/mês
API Requests (50.000/mês):                $0.15/mês
CloudHSM (1 cluster, 730h/mês):       $1.058,50/mês
───────────────────────────────────────────────────────
Total:                                 $1.062,65/mês
```

**Otimização:** Usar KMS Standard para Root CA (ao invés de CloudHSM) economiza ~$1.000/mês, mas reduz segurança de FIPS 140-2 Level 3 para Level 2.

## Próximos passos

No próximo capítulo, você aprenderá a configurar **AWS Private CA** integrada com as chaves KMS criadas aqui, incluindo:

- Hierarquia de CA (Root + Intermediates)
- Emissão de certificados
- Configuração de OCSP e CRL
- Integração com ACM

## Referências

- **AWS KMS Developer Guide:** [https://docs.aws.amazon.com/kms/](https://docs.aws.amazon.com/kms/)
- **KMS Best Practices:** [https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html](https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html)
- **KMS Cryptographic Details:** [https://docs.aws.amazon.com/kms/latest/cryptographic-details/](https://docs.aws.amazon.com/kms/latest/cryptographic-details/)

