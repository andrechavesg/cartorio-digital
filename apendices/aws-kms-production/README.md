# Apêndice – Getting Production Ready with AWS KMS

Este apêndice apresenta um guia completo para levar a infraestrutura de PKI do **Cartório Digital** para produção na AWS, utilizando serviços gerenciados como **AWS KMS (Key Management Service)**, **AWS Private CA**, **AWS Secrets Manager** e outros componentes críticos para um ambiente de produção robusto, seguro e escalável.

## Sumário dos capítulos

1. [Arquitetura de referência para produção](01_arquitetura_producao.md) – Design de infraestrutura PKI resiliente na AWS
2. [AWS KMS para proteção de chaves](02_aws_kms.md) – Gerenciamento seguro de chaves criptográficas
3. [AWS Private CA](03_aws_private_ca.md) – Autoridade Certificadora gerenciada
4. [AWS Secrets Manager](04_secrets_manager.md) – Gestão de certificados e credenciais
5. [Automação com Infrastructure as Code](05_iac_terraform.md) – Terraform para provisionamento
6. [Segurança e Conformidade](06_seguranca_conformidade.md) – Hardening e auditorias
7. [Monitoramento e Observabilidade](07_monitoramento.md) – CloudWatch, CloudTrail e alarmes
8. [Disaster Recovery e Backup](08_disaster_recovery.md) – Continuidade de negócio

## Visão geral

Ao longo dos módulos do curso, construímos o Cartório Digital usando uma combinação de ferramentas open source, serviços AWS e boas práticas de segurança. Este apêndice consolida todo esse conhecimento em um **guia de produção prático**, cobrindo:

### 🏗️ Infraestrutura

- **Arquitetura multi-AZ** para alta disponibilidade
- **VPC isolada** com subnets públicas e privadas
- **Hierarquia de CA** (Root CA offline + Intermediate CAs)
- **Load balancers** com certificados gerenciados

### 🔐 Segurança

- **AWS KMS** para proteção de chaves raiz e intermediárias
- **HSM (CloudHSM)** para workloads críticos
- **IAM policies** granulares com least privilege
- **Encryption at-rest** e **in-transit** em todos os componentes
- **AWS WAF** para proteção de APIs públicas

### 🤖 Automação

- **Terraform** para Infrastructure as Code
- **CI/CD pipelines** com CodePipeline/GitHub Actions
- **Renovação automática** de certificados via ACME
- **Lambda functions** para tarefas operacionais

### 📊 Observabilidade

- **CloudWatch Logs** agregados e estruturados
- **CloudWatch Metrics** customizadas para PKI
- **CloudTrail** para auditoria completa
- **SNS/SES** para alertas críticos
- **Dashboards** para visibilidade operacional

### 🔄 Resiliência

- **Backups automáticos** multi-região
- **RTO/RPO** definidos e testados
- **Runbooks** documentados para incidentes
- **Chaos engineering** para validação

## Por que AWS para produção?

### ✅ Vantagens

**1. Serviços gerenciados**
- Menos overhead operacional
- Patches e atualizações automáticas
- SLAs garantidos (99.9% ou superior)

**2. Segurança nativa**
- Conformidade com SOC 2, PCI DSS, HIPAA, ISO 27001
- Encryption por padrão
- Integração com AWS Security Hub

**3. Escalabilidade**
- Auto-scaling automático
- Sem planejamento de capacidade
- Pay-per-use

**4. Integração**
- APIs consistentes entre serviços
- IAM unificado
- CloudFormation/Terraform suportado

**5. Suporte**
- AWS Support (Business/Enterprise)
- Documentação extensiva
- Comunidade ativa

### ⚠️ Considerações

**1. Custos**
- Pode ser mais caro que self-hosted para alto volume
- Necessário monitoramento de custos (AWS Cost Explorer)

**2. Vendor lock-in**
- Migração para outro cloud requer esforço
- Mitigação: usar abstrações (Terraform, containers)

**3. Soberania de dados**
- Certificar que região AWS atende requisitos legais
- Para Brasil: usar região `sa-east-1` (São Paulo)

**4. Complexidade inicial**
- Curva de aprendizado de serviços AWS
- Necessário conhecimento de IAM, VPC, etc.

## Comparação: Self-hosted vs AWS Managed

| Aspecto | Self-hosted (EJBCA-CE) | AWS Managed |
|---------|------------------------|-------------|
| **Setup inicial** | Semanas | Dias |
| **Custo mensal (10k certs)** | ~$1.500 | ~$1.200 |
| **Operação** | Equipe DevOps dedicada | Mínima |
| **Escalabilidade** | Manual | Automática |
| **SLA** | Você garante | AWS garante (99.9%+) |
| **Conformidade** | Você implementa | Built-in |
| **Customização** | Máxima | Média |
| **Vendor lock-in** | Nenhum | Alto |
| **On-premises** | ✅ Sim | ❌ Não |

## Arquitetura de referência

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud (sa-east-1)                   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                     VPC (10.0.0.0/16)                     │ │
│  │                                                           │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │              Public Subnets (Multi-AZ)              │ │ │
│  │  │  ┌──────────────┐         ┌──────────────┐         │ │ │
│  │  │  │ ALB (HTTPS)  │         │  NAT Gateway │         │ │ │
│  │  │  └──────────────┘         └──────────────┘         │ │ │
│  │  └─────────────────────────────────────────────────────┘ │ │
│  │                            │                              │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │             Private Subnets (Multi-AZ)              │ │ │
│  │  │                                                     │ │ │
│  │  │  ┌──────────────────────────────────────────────┐  │ │ │
│  │  │  │      ECS/Fargate (Backend API)              │  │ │ │
│  │  │  │  - FastAPI com mTLS                         │  │ │ │
│  │  │  │  - Certificados do Private CA               │  │ │ │
│  │  │  └──────────────────────────────────────────────┘  │ │ │
│  │  │                                                     │ │ │
│  │  │  ┌──────────────────────────────────────────────┐  │ │ │
│  │  │  │    RDS PostgreSQL (Multi-AZ)                │  │ │ │
│  │  │  │  - Metadados de certificados                │  │ │ │
│  │  │  │  - Encryption at-rest (KMS)                 │  │ │ │
│  │  │  └──────────────────────────────────────────────┘  │ │ │
│  │  │                                                     │ │ │
│  │  └─────────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                  Managed Services                         │ │
│  │                                                           │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │ │
│  │  │   AWS KMS    │  │ Private CA   │  │   Secrets    │   │ │
│  │  │  (CMK Keys)  │  │ (Root + Int) │  │   Manager    │   │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │ │
│  │                                                           │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │ │
│  │  │  CloudWatch  │  │  CloudTrail  │  │  S3 Backup   │   │ │
│  │  │  (Metrics)   │  │  (Audit Log) │  │  (Multi-AZ)  │   │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │ │
│  │                                                           │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Fluxo de emissão de certificado em produção

```
┌──────────────┐
│   Cliente    │
│ (Navegador/  │
│   API)       │
└──────┬───────┘
       │ 1. HTTPS Request
       │    (TLS cert do ALB via ACM)
       ▼
┌──────────────────┐
│  ALB + WAF       │
│  - DDoS protect  │
│  - Rate limiting │
└──────┬───────────┘
       │ 2. mTLS
       │    (Client cert validation)
       ▼
┌────────────────────────┐
│  Backend API (ECS)     │
│  - Valida JWT          │
│  - Verifica IAM perms  │
└──────┬─────────────────┘
       │ 3. Solicita cert
       │
       ├─────────────────────────────┐
       │                             │
       ▼                             ▼
┌──────────────┐           ┌──────────────────┐
│ AWS Private  │           │  RDS PostgreSQL  │
│     CA       │           │  - Metadata      │
│ - Emite cert │◄─────────►│  - Audit log     │
└──────┬───────┘           └──────────────────┘
       │ 4. Cert emitido
       │    (assinado por Intermediate CA)
       ▼
┌──────────────────┐
│  Secrets Manager │
│  - Armazena cert │
│  - Rotação auto  │
└──────┬───────────┘
       │ 5. Cert + chave privada
       │    (encrypted with KMS)
       ▼
┌──────────────┐
│   Cliente    │
│  (recebe P12)│
└──────────────┘
```

## Custos estimados (produção)

### Cenário: Cartório Digital com 10.000 certificados/ano

| Serviço | Configuração | Custo mensal (USD) |
|---------|-------------|-------------------|
| **AWS Private CA** | 1 Root CA (offline após setup) | $400 |
| | 2 Intermediate CAs (active) | $800 |
| | Certificados emitidos (833/mês) | $625 |
| **AWS KMS** | 3 CMKs (CA keys) | $3 |
| | 10.000 API calls/mês | $0.03 |
| **ECS Fargate** | 2 tasks (1 vCPU, 2GB) | $30 |
| **RDS PostgreSQL** | db.t3.small Multi-AZ | $50 |
| **ALB** | 1 ALB + 10GB/mês | $20 |
| **S3** | 100GB backup + transfer | $5 |
| **Secrets Manager** | 100 secrets | $40 |
| **CloudWatch** | Logs + Metrics | $20 |
| **CloudTrail** | Management events | $5 |
| **Data Transfer** | 50GB out/mês | $5 |
| | | |
| **Total mensal** | | **~$2.003** |
| **Total anual** | | **~$24.036** |
| **Custo por certificado** | | **$2.40** |

### Otimizações de custo

1. **Usar Single Root CA**: Economizar $400/mês (se hierarquia simples for suficiente)
2. **Fargate Spot**: Reduzir custo de compute em até 70%
3. **Reserved Instances** para RDS: Economizar ~30% com commitment de 1 ano
4. **S3 Intelligent-Tiering**: Reduzir custos de storage em ~40%
5. **Consolidar Secrets**: Agrupar certificados relacionados

**Com otimizações:** ~$1.200/mês (~50% economia)

## Conformidade e certificações AWS

A infraestrutura na AWS herda diversas certificações:

- ✅ **SOC 2 Type II**
- ✅ **PCI DSS Level 1**
- ✅ **ISO 27001, 27017, 27018**
- ✅ **HIPAA** (requer BAA)
- ✅ **FedRAMP Moderate/High**
- ✅ **LGPD** (Brasil)

Para o **Cartório Digital**, isso facilita auditorias de conformidade com:
- ICP-Brasil (DOC-ICP-01 a DOC-ICP-15)
- LGPD (Lei 13.709/2018)
- Normas do CNJ (Conselho Nacional de Justiça)

## Integração com módulos do curso

Este apêndice integra conhecimentos de:

- **Módulo 2 (PKI):** AWS Private CA implementa hierarquias de CA
- **Módulo 3 (TLS/mTLS):** ACM + ALB para TLS, Private CA para mTLS
- **Módulo 4 (ACME):** ACM suporta ACME para renovação automática
- **Módulo 6 (KMS/HSM):** AWS KMS e CloudHSM para proteção de chaves
- **Módulo 8 (Cloud/CI/CD):** CodePipeline, Terraform, GitOps
- **Módulo 9 (Observabilidade):** CloudWatch, CloudTrail, X-Ray

## Pré-requisitos

Antes de seguir este apêndice, você deve ter:

1. ✅ **Conta AWS** com permissões de administrador
2. ✅ **AWS CLI** instalado e configurado
3. ✅ **Terraform** (versão 1.5+) instalado
4. ✅ Conhecimento dos **módulos 1-10** do curso
5. ✅ **Orçamento** definido (Cost Budget no AWS Cost Explorer)

## Próximos passos

1. Leia o **Capítulo 1** para entender a arquitetura de referência completa
2. Siga o **Capítulo 2** para configurar AWS KMS e proteger suas chaves
3. Configure **AWS Private CA** no Capítulo 3
4. Implemente **Infrastructure as Code** com Terraform (Capítulo 5)
5. Configure **monitoramento** e alertas (Capítulo 7)
6. Teste **Disaster Recovery** (Capítulo 8)

## Recursos adicionais

- **AWS Well-Architected Framework:** [https://aws.amazon.com/architecture/well-architected/](https://aws.amazon.com/architecture/well-architected/)
- **AWS Security Best Practices:** [https://docs.aws.amazon.com/security/](https://docs.aws.amazon.com/security/)
- **AWS Private CA Documentation:** [https://docs.aws.amazon.com/privateca/](https://docs.aws.amazon.com/privateca/)
- **AWS KMS Best Practices:** [https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html](https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html)

---

**Nota:** Este apêndice é complementar ao curso e assume conhecimento dos módulos principais. Use-o como guia de implementação para ambientes de produção, adaptando conforme necessidades específicas do seu projeto.

