# Capítulo 1 – Arquitetura de Referência para Produção

## Princípios de design

A arquitetura de produção do Cartório Digital na AWS segue os **AWS Well-Architected Framework Pillars**:

### 1. Operational Excellence (Excelência Operacional)
- Infrastructure as Code (Terraform)
- CI/CD automatizado
- Runbooks documentados
- Monitoramento proativo

### 2. Security (Segurança)
- Least privilege (IAM policies granulares)
- Defense in depth (múltiplas camadas de segurança)
- Encryption everywhere (at-rest + in-transit)
- Auditoria completa (CloudTrail)

### 3. Reliability (Confiabilidade)
- Multi-AZ deployment
- Auto-healing (ECS health checks)
- Backups automáticos
- Tested disaster recovery

### 4. Performance Efficiency (Eficiência de Desempenho)
- Serverless onde possível (Lambda, Fargate)
- Caching estratégico (ElastiCache)
- CDN para assets estáticos (CloudFront)

### 5. Cost Optimization (Otimização de Custos)
- Right-sizing de recursos
- Auto-scaling
- Reserved Instances para workloads previsíveis
- Cost monitoring e alertas

### 6. Sustainability (Sustentabilidade)
- Uso de regiões com energia renovável
- Otimização de recursos (menos compute = menos carbono)

## Arquitetura completa

### Diagrama de rede

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AWS Region: sa-east-1 (São Paulo)                │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │                 VPC: cartorio-prod (10.0.0.0/16)              │ │
│  │                                                               │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │              Availability Zone 1a                       │ │ │
│  │  │                                                         │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Public Subnet (10.0.1.0/24)                     │  │ │ │
│  │  │  │  - ALB (Application Load Balancer)               │  │ │ │
│  │  │  │  - NAT Gateway                                   │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  │                         │                               │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Private Subnet App (10.0.11.0/24)               │  │ │ │
│  │  │  │  - ECS Tasks (Backend API)                       │  │ │ │
│  │  │  │  - Lambda functions                              │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  │                         │                               │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Private Subnet Data (10.0.21.0/24)              │  │ │ │
│  │  │  │  - RDS PostgreSQL (Primary)                      │  │ │ │
│  │  │  │  - ElastiCache Redis                             │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                               │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │              Availability Zone 1b                       │ │ │
│  │  │                                                         │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Public Subnet (10.0.2.0/24)                     │  │ │ │
│  │  │  │  - ALB (standby)                                 │  │ │ │
│  │  │  │  - NAT Gateway (standby)                         │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  │                         │                               │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Private Subnet App (10.0.12.0/24)               │  │ │ │
│  │  │  │  - ECS Tasks (Backend API)                       │  │ │ │
│  │  │  │  - Lambda functions                              │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  │                         │                               │ │ │
│  │  │  ┌──────────────────────────────────────────────────┐  │ │ │
│  │  │  │  Private Subnet Data (10.0.22.0/24)              │  │ │ │
│  │  │  │  - RDS PostgreSQL (Standby)                      │  │ │ │
│  │  │  │  - ElastiCache Redis (Replica)                   │  │ │ │
│  │  │  └──────────────────────────────────────────────────┘  │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                               │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Componentes de compute

```
┌────────────────────────────────────────────────────────────┐
│                   Compute Layer                            │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │            ECS Cluster (Fargate)                     │ │
│  │                                                      │ │
│  │  ┌────────────────┐    ┌────────────────┐          │ │
│  │  │ Backend API    │    │ Worker Service │          │ │
│  │  │ (FastAPI)      │    │ (Cert Renewal) │          │ │
│  │  │ - 2 tasks      │    │ - 1 task       │          │ │
│  │  │ - 1 vCPU       │    │ - 0.5 vCPU     │          │ │
│  │  │ - 2 GB RAM     │    │ - 1 GB RAM     │          │ │
│  │  └────────────────┘    └────────────────┘          │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │            Lambda Functions                          │ │
│  │                                                      │ │
│  │  • cert-expiry-check (daily)                        │ │
│  │  • backup-trigger (hourly)                          │ │
│  │  • audit-log-processor (event-driven)               │ │
│  │  • ocsp-responder (on-demand)                       │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Camada de dados

```
┌────────────────────────────────────────────────────────────┐
│                    Data Layer                              │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │    RDS PostgreSQL 15 (db.t3.medium)                  │ │
│  │                                                      │ │
│  │  Primary (AZ-1a)          Standby (AZ-1b)           │ │
│  │  ┌──────────────┐         ┌──────────────┐          │ │
│  │  │  PostgreSQL  │────────►│  PostgreSQL  │          │ │
│  │  │   (Master)   │ Sync    │  (Replica)   │          │ │
│  │  └──────────────┘ Replic. └──────────────┘          │ │
│  │                                                      │ │
│  │  Armazena:                                          │ │
│  │  - Metadados de certificados                        │ │
│  │  - Audit logs                                       │ │
│  │  - Usuários e permissões                            │ │
│  │                                                      │ │
│  │  Backup automático: Diário (retention: 30 dias)     │ │
│  │  Encryption at-rest: KMS CMK                        │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │    ElastiCache Redis 7 (cache.t3.small)              │ │
│  │                                                      │ │
│  │  Primary (AZ-1a)          Replica (AZ-1b)           │ │
│  │  ┌──────────────┐         ┌──────────────┐          │ │
│  │  │    Redis     │────────►│    Redis     │          │ │
│  │  │  (Primary)   │ Async   │  (Replica)   │          │ │
│  │  └──────────────┘ Replic. └──────────────┘          │ │
│  │                                                      │ │
│  │  Cache de:                                          │ │
│  │  - Sessões de usuário                               │ │
│  │  - OCSP responses                                   │ │
│  │  - Configurações frequentes                         │ │
│  │                                                      │ │
│  │  TTL: 5 minutos (OCSP), 1 hora (config)            │ │
│  │  Encryption in-transit: TLS 1.3                     │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │    S3 Buckets                                        │ │
│  │                                                      │ │
│  │  • cartorio-prod-certificates (certs emitidos)      │ │
│  │  • cartorio-prod-backups (backups PostgreSQL)       │ │
│  │  • cartorio-prod-audit-logs (CloudTrail logs)       │ │
│  │  • cartorio-prod-access-logs (ALB/CloudFront logs)  │ │
│  │                                                      │ │
│  │  Encryption: SSE-KMS                                │ │
│  │  Versioning: Enabled                                │ │
│  │  Lifecycle: 90d → Glacier, 7y → Delete             │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Serviços gerenciados AWS

```
┌────────────────────────────────────────────────────────────┐
│                 AWS Managed Services                       │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                   AWS KMS                            │ │
│  │                                                      │ │
│  │  CMK Keys:                                          │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │ cartorio-root-ca-key (RSA 4096)                │ │ │
│  │  │ - Protected with CloudHSM                      │ │ │
│  │  │ - Auto-rotation: Disabled (manual control)     │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │ cartorio-intermediate-ca-key (RSA 2048)        │ │ │
│  │  │ - Auto-rotation: Annual                        │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │ cartorio-data-encryption-key                   │ │ │
│  │  │ - For RDS, S3, Secrets Manager                 │ │ │
│  │  │ - Auto-rotation: Enabled                       │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              AWS Private CA                          │ │
│  │                                                      │ │
│  │  Hierarchy:                                         │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │ Root CA (offline após setup)                   │ │ │
│  │  │ CN=Cartorio Digital Root CA                    │ │ │
│  │  │ Validity: 20 years                             │ │ │
│  │  │ Key: RSA 4096 (KMS protected)                  │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │                    │                                 │ │
│  │         ┌──────────┴──────────┐                     │ │
│  │         ▼                     ▼                     │ │
│  │  ┌─────────────┐       ┌─────────────┐             │ │
│  │  │ TLS CA      │       │ Document    │             │ │
│  │  │ (active)    │       │ Signing CA  │             │ │
│  │  │ 5y validity │       │ (active)    │             │ │
│  │  └─────────────┘       │ 5y validity │             │ │
│  │                        └─────────────┘             │ │
│  │                                                      │ │
│  │  OCSP Responder: Enabled                            │ │
│  │  CRL: Published to S3 every 24h                     │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │            AWS Secrets Manager                       │ │
│  │                                                      │ │
│  │  Secrets:                                           │ │
│  │  • cartorio/prod/db-credentials                     │ │
│  │  • cartorio/prod/jwt-signing-key                    │ │
│  │  • cartorio/prod/api-keys/*                         │ │
│  │  • cartorio/prod/service-certs/*                    │ │
│  │                                                      │ │
│  │  Rotation: Automatic (30 days)                      │ │
│  │  Encryption: KMS CMK                                │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                  ACM (Certificate Manager)           │ │
│  │                                                      │ │
│  │  Certificados TLS para:                             │ │
│  │  • ALB (*.cartorio.gov.br)                          │ │
│  │  • CloudFront (portal.cartorio.gov.br)              │ │
│  │  • API Gateway (api.cartorio.gov.br)                │ │
│  │                                                      │ │
│  │  Renovação: Automática (via ACM)                    │ │
│  │  Validação: DNS (Route 53)                          │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

## Fluxos críticos

### 1. Emissão de certificado de documento

```
1. Usuário autentica (OAuth2 + JWT)
2. Backend valida permissões (IAM + custom RBAC)
3. Gera CSR com dados do documento
4. Solicita cert ao Private CA (Intermediate "Document Signing CA")
5. Private CA assina certificado
6. Backend assina documento PDF com certificado
7. Adiciona timestamp (RFC 3161)
8. Armazena PDF assinado no S3
9. Registra auditoria no RDS + CloudTrail
10. Retorna PDF + link de download ao usuário
```

### 2. Renovação automática de certificado TLS

```
1. Lambda "cert-expiry-check" roda diariamente
2. Consulta ACM + Private CA para certs próximos da expiração
3. Para certs ACM: Renovação automática (nada a fazer)
4. Para certs Private CA:
   a. Gera novo CSR
   b. Solicita novo cert
   c. Atualiza Secrets Manager
   d. Dispara CodeDeploy para aplicar novo cert
5. Envia notificação SNS se houver falhas
```

### 3. Validação OCSP

```
1. Cliente consulta status de certificado (HTTPS GET)
2. ALB roteia para Lambda "ocsp-responder"
3. Lambda verifica cache Redis
4. Se miss: Consulta Private CA OCSP endpoint
5. Armazena response no Redis (TTL: 5min)
6. Retorna response assinada ao cliente
```

## Segurança em camadas

### Camada 1: Rede
- **VPC isolada** com subnets privadas
- **Security Groups** restritivos (whitelist only)
- **NACLs** como segunda barreira
- **VPC Flow Logs** para análise de tráfego
- **AWS WAF** no ALB (proteção DDoS, SQL injection, XSS)

### Camada 2: Identidade e acesso
- **IAM Roles** para cada serviço (sem access keys hard-coded)
- **Policies de least privilege**
- **MFA obrigatório** para usuários humanos
- **SCPs (Service Control Policies)** no nível de Organization
- **AWS SSO** para acesso federado

### Camada 3: Dados
- **Encryption at-rest** (KMS) para RDS, S3, EBS
- **Encryption in-transit** (TLS 1.3) para todas as comunicações
- **Field-level encryption** para dados sensíveis (CPF, etc.)
- **Database column encryption** para PII

### Camada 4: Aplicação
- **mTLS** entre serviços internos
- **JWT com rotação** de signing keys
- **Rate limiting** (AWS WAF + API Gateway)
- **Input validation** rigorosa
- **OWASP Top 10** mitigado

### Camada 5: Auditoria
- **CloudTrail** para todas as API calls
- **VPC Flow Logs** para tráfego de rede
- **Application logs** estruturados (JSON) no CloudWatch
- **AWS Config** para compliance tracking
- **GuardDuty** para detecção de ameaças

## Alta disponibilidade

### RTO (Recovery Time Objective): 1 hora
### RPO (Recovery Point Objective): 15 minutos

**Estratégias:**

1. **Multi-AZ deployment**
   - RDS: Failover automático (< 2 minutos)
   - ECS: Tasks distribuídas em múltiplas AZs
   - ALB: Automaticamente Multi-AZ

2. **Auto-healing**
   - ECS health checks: Reinicia tasks não saudáveis
   - ALB health checks: Remove targets não saudáveis
   - Lambda: Retry automático em falhas

3. **Backups**
   - RDS: Snapshots diários + PITR (Point-in-Time Recovery)
   - S3: Versionamento + Cross-Region Replication (opcional)
   - Secrets Manager: Histórico de versões

## Próximos passos

No próximo capítulo, você aprenderá a configurar **AWS KMS** para proteção das chaves de CA, incluindo:

- Criação de CMKs (Customer Master Keys)
- Políticas de acesso granulares
- Integração com CloudHSM para chaves raiz
- Auditoria de uso de chaves
- Rotação automática de chaves

## Referências

- **AWS Well-Architected Framework:** [https://aws.amazon.com/architecture/well-architected/](https://aws.amazon.com/architecture/well-architected/)
- **AWS Multi-AZ Deployments:** [https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.MultiAZ.html](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.MultiAZ.html)
- **AWS Security Best Practices:** [https://docs.aws.amazon.com/security/](https://docs.aws.amazon.com/security/)

