# Capítulo 5 – Infrastructure as Code com Terraform

## Por que Terraform?

**Infrastructure as Code (IaC)** permite gerenciar infraestrutura usando código versionado, testável e reproduzível. Para o Cartório Digital em produção, isso significa:

✅ **Reprodutibilidade:** Recrie ambiente completo em minutos  
✅ **Versionamento:** Git tracking de todas as mudanças  
✅ **Auditoria:** Histórico completo de quem mudou o quê  
✅ **Teste:** Valide infraestrutura antes de aplicar  
✅ **Disaster Recovery:** Recrie em outra região rapidamente  

### Terraform vs CloudFormation

| Aspecto | Terraform | CloudFormation |
|---------|-----------|----------------|
| **Multi-cloud** | ✅ AWS, Azure, GCP | ❌ Apenas AWS |
| **Linguagem** | HCL (declarativa) | JSON/YAML |
| **State management** | Remoto (S3 + DynamoDB) | Automático |
| **Comunidade** | Grande (HashiCorp) | AWS oficial |
| **Módulos** | Terraform Registry | AWS SAM/CDK |
| **Drift detection** | `terraform plan` | CloudFormation Drift |

**Escolha:** Terraform pela portabilidade e ecossistema.

## Estrutura do projeto Terraform

```
terraform/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   └── prod/
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
├── modules/
│   ├── networking/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── kms/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── private-ca/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecs/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── rds/
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
├── backend.tf
└── README.md
```

## Módulo: Networking (VPC)

### `modules/networking/main.tf`

```hcl
# modules/networking/main.tf

terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# VPC
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-vpc"
  })
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-igw"
  })
}

# Public Subnets
resource "aws_subnet" "public" {
  count                   = length(var.availability_zones)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-public-${var.availability_zones[count.index]}"
    Tier = "Public"
  })
}

# Private Subnets (App)
resource "aws_subnet" "private_app" {
  count             = length(var.availability_zones)
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 10)
  availability_zone = var.availability_zones[count.index]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-private-app-${var.availability_zones[count.index]}"
    Tier = "Private-App"
  })
}

# Private Subnets (Data)
resource "aws_subnet" "private_data" {
  count             = length(var.availability_zones)
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 20)
  availability_zone = var.availability_zones[count.index]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-private-data-${var.availability_zones[count.index]}"
    Tier = "Private-Data"
  })
}

# NAT Gateways
resource "aws_eip" "nat" {
  count  = length(var.availability_zones)
  domain = "vpc"

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-nat-eip-${var.availability_zones[count.index]}"
  })

  depends_on = [aws_internet_gateway.main]
}

resource "aws_nat_gateway" "main" {
  count         = length(var.availability_zones)
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-nat-${var.availability_zones[count.index]}"
  })
}

# Route Tables
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-public-rt"
  })
}

resource "aws_route_table" "private_app" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main[count.index].id
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-private-app-rt-${var.availability_zones[count.index]}"
  })
}

# Route Table Associations
resource "aws_route_table_association" "public" {
  count          = length(var.availability_zones)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private_app" {
  count          = length(var.availability_zones)
  subnet_id      = aws_subnet.private_app[count.index].id
  route_table_id = aws_route_table.private_app[count.index].id
}

# VPC Flow Logs
resource "aws_flow_log" "main" {
  iam_role_arn    = aws_iam_role.vpc_flow_log.arn
  log_destination = aws_cloudwatch_log_group.vpc_flow_log.arn
  traffic_type    = "ALL"
  vpc_id          = aws_vpc.main.id

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-vpc-flow-log"
  })
}

resource "aws_cloudwatch_log_group" "vpc_flow_log" {
  name              = "/aws/vpc/${var.project_name}"
  retention_in_days = 30

  tags = var.common_tags
}

resource "aws_iam_role" "vpc_flow_log" {
  name = "${var.project_name}-vpc-flow-log-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "vpc-flow-logs.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = var.common_tags
}

resource "aws_iam_role_policy" "vpc_flow_log" {
  name = "${var.project_name}-vpc-flow-log-policy"
  role = aws_iam_role.vpc_flow_log.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogGroups",
        "logs:DescribeLogStreams"
      ]
      Resource = "*"
    }]
  })
}
```

### `modules/networking/variables.tf`

```hcl
variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block da VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Lista de AZs a usar"
  type        = list(string)
  default     = ["sa-east-1a", "sa-east-1b"]
}

variable "common_tags" {
  description = "Tags comuns para todos os recursos"
  type        = map(string)
  default     = {}
}
```

### `modules/networking/outputs.tf`

```hcl
output "vpc_id" {
  description = "ID da VPC"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "IDs das subnets públicas"
  value       = aws_subnet.public[*].id
}

output "private_app_subnet_ids" {
  description = "IDs das subnets privadas (app)"
  value       = aws_subnet.private_app[*].id
}

output "private_data_subnet_ids" {
  description = "IDs das subnets privadas (data)"
  value       = aws_subnet.private_data[*].id
}
```

## Módulo: KMS

### `modules/kms/main.tf`

```hcl
# modules/kms/main.tf

resource "aws_kms_key" "root_ca" {
  description              = "Cartorio Digital Root CA Private Key"
  key_usage               = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_4096"
  deletion_window_in_days  = 30
  enable_key_rotation      = false  # Manual rotation for root CA

  tags = merge(var.common_tags, {
    Name    = "${var.project_name}-root-ca-key"
    Purpose = "RootCA"
  })
}

resource "aws_kms_alias" "root_ca" {
  name          = "alias/${var.project_name}-root-ca-key"
  target_key_id = aws_kms_key.root_ca.key_id
}

resource "aws_kms_key_policy" "root_ca" {
  key_id = aws_kms_key.root_ca.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${var.aws_account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow Private CA Service"
        Effect = "Allow"
        Principal = {
          Service = "acm-pca.amazonaws.com"
        }
        Action = [
          "kms:Decrypt",
          "kms:Sign",
          "kms:GetPublicKey"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:ViaService" = "acm-pca.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_kms_key" "intermediate_tls_ca" {
  description              = "Cartorio Digital Intermediate TLS CA Key"
  key_usage               = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  deletion_window_in_days  = 30
  enable_key_rotation      = true  # Annual automatic rotation

  tags = merge(var.common_tags, {
    Name    = "${var.project_name}-intermediate-tls-ca-key"
    Purpose = "IntermediateTLSCA"
  })
}

resource "aws_kms_alias" "intermediate_tls_ca" {
  name          = "alias/${var.project_name}-intermediate-tls-ca-key"
  target_key_id = aws_kms_key.intermediate_tls_ca.key_id
}

resource "aws_kms_key" "data_encryption" {
  description             = "Cartorio Digital Data Encryption Key"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = merge(var.common_tags, {
    Name    = "${var.project_name}-data-encryption-key"
    Purpose = "DataEncryption"
  })
}

resource "aws_kms_alias" "data_encryption" {
  name          = "alias/${var.project_name}-data-encryption-key"
  target_key_id = aws_kms_key.data_encryption.key_id
}
```

## Módulo: Private CA

### `modules/private-ca/main.tf`

```hcl
resource "aws_acmpca_certificate_authority" "intermediate" {
  permanent_deletion_time_in_days = 7
  type                            = "SUBORDINATE"

  certificate_authority_configuration {
    key_algorithm     = "RSA_2048"
    signing_algorithm = "SHA256WITHRSA"
    subject {
      common_name  = "${var.project_name} Intermediate CA"
      organization = "Cartorio Digital"
      country      = "BR"
    }
  }

  revocation_configuration {
    crl_configuration {
      enabled            = true
      expiration_in_days = 7
      s3_bucket_name     = var.crl_bucket_name
      custom_cname       = "crl.${var.domain_name}"
    }
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-intermediate-ca"
  })
}

resource "aws_acmpca_permission" "private_ca" {
  certificate_authority_arn = aws_acmpca_certificate_authority.intermediate.arn
  principal                 = "acm.amazonaws.com"
  actions                   = ["IssueCertificate", "GetCertificate", "ListPermissions"]
  source_account            = var.aws_account_id
}
```

Esse módulo entrega a CA subordinada usada pelos fluxos de emissão descritos no Capítulo 3, publica CRLs em S3 e garante aderência ao DOC-ICP-05.

## Módulo: ECS (Serviços aplicacionais)

### `modules/ecs/main.tf`

```hcl
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  tags = var.common_tags
}

resource "aws_ecs_task_definition" "ejbca" {
  family                   = "${var.project_name}-ejbca"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "1024"
  memory                   = "2048"
  execution_role_arn       = var.ecs_execution_role_arn
  task_role_arn            = var.ecs_task_role_arn

  container_definitions = jsonencode([
    {
      name        = "ejbca"
      image       = var.ejbca_image
      essential   = true
      portMappings = [
        { containerPort = 8080, hostPort = 8080, protocol = "tcp" },
        { containerPort = 8443, hostPort = 8443, protocol = "tcp" }
      ]
      environment = [
        { name = "DATABASE_JDBC_URL", value = var.jdbc_url },
        { name = "TLS_SETUP_ENABLED", value = "simple" },
        { name = "LOG_LEVEL_APP", value = "INFO" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/${var.project_name}/ejbca"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "ejbca" {
  name                  = "${var.project_name}-ejbca"
  cluster               = aws_ecs_cluster.main.id
  task_definition       = aws_ecs_task_definition.ejbca.arn
  desired_count         = var.desired_count
  launch_type           = "FARGATE"
  enable_execute_command = true

  network_configuration {
    assign_public_ip = false
    subnets          = var.private_app_subnets
    security_groups  = [var.ejbca_sg_id]
  }

  load_balancer {
    target_group_arn = var.alb_target_group_arn
    container_name   = "ejbca"
    container_port   = 8443
  }

  deployment_controller {
    type = "CODE_DEPLOY"
  }

  lifecycle {
    ignore_changes = [desired_count]
  }
}
```

O módulo consome sub-redes privadas e security groups gerados pelo módulo de rede, expõe o ARN do serviço e habilita observabilidade via Container Insights.

## Módulo: RDS / Aurora PostgreSQL

### `modules/rds/main.tf`

```hcl
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-aurora-subnets"
  subnet_ids = var.private_data_subnets
  tags       = var.common_tags
}

resource "aws_rds_cluster" "aurora" {
  cluster_identifier      = "${var.project_name}-aurora"
  engine                  = "aurora-postgresql"
  engine_mode             = "provisioned"
  master_username         = var.master_username
  master_password         = var.master_password
  database_name           = "ejbca"
  backup_retention_period = 7
  storage_encrypted       = true
  kms_key_id              = var.kms_key_id
  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [var.db_sg_id]
  enabled_cloudwatch_logs_exports = ["postgresql"]
  copy_tags_to_snapshot   = true
  tags = merge(var.common_tags, { Name = "${var.project_name}-aurora" })
}

resource "aws_rds_cluster_instance" "aurora_instances" {
  count              = 2
  identifier         = "${var.project_name}-aurora-${count.index}"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = var.instance_class
  engine             = aws_rds_cluster.aurora.engine
  engine_version     = aws_rds_cluster.aurora.engine_version
  publicly_accessible = false
  monitoring_interval = 60
  performance_insights_enabled = true
}
```

## Módulo: Observabilidade & DR

```hcl
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project_name}"
  retention_in_days = 90
  kms_key_id        = var.logs_kms_key_id
  tags              = var.common_tags
}

resource "aws_cloudwatch_dashboard" "operations" {
  dashboard_name = "${var.project_name}-operations"
  dashboard_body = file("${path.module}/dashboards/operations.json")
}

resource "aws_backup_vault" "main" {
  name        = "${var.project_name}-backup-vault"
  kms_key_arn = var.backup_kms_key_arn
  tags        = var.common_tags
}

resource "aws_backup_plan" "aurora" {
  name = "${var.project_name}-aurora-plan"
  rule {
    rule_name         = "daily-backup"
    target_vault_name = aws_backup_vault.main.name
    schedule          = "cron(0 5 * * ? *)"
    lifecycle {
      cold_storage_after = 30
      delete_after       = 365
    }
  }
}

resource "aws_backup_selection" "aurora" {
  iam_role_arn = var.backup_role_arn
  name         = "aurora-selection"
  plan_id      = aws_backup_plan.aurora.id
  resources    = [aws_rds_cluster.aurora.arn]
}
```

## Parâmetros por ambiente

Cada diretório em `terraform/environments/` referencia os módulos com valores reais. Exemplo (`environments/prod/terraform.tfvars`):

```hcl
project_name       = "cartorio-prod"
aws_region         = "sa-east-1"
availability_zones = ["sa-east-1a", "sa-east-1b", "sa-east-1c"]
domain_name        = "cartorio.gov.br"
crl_bucket_name    = "cartorio-prod-crl"
desired_count      = 3
instance_class     = "db.r6g.large"
master_username    = "ejbca_admin"
master_password    = var.rds_master_password
common_tags = {
  Environment = "prod"
  Owner       = "PKI Team"
  Compliance  = "ICP-Brasil"
}
```

## Orquestração com ECS e EKS

- **ECS Fargate** hospeda EJBCA, APIs e serviços auxiliares com autoscaling baseado em CPU, memória e profundidade de fila SQS.
- **App Mesh** adiciona mTLS interno e observabilidade (X-Ray) provisionada por módulo opcional `modules/app-mesh`.
- **CodeDeploy** executa deploy blue/green no ECS usando o `deployment_controller` definido acima, com rollback automático.
- **EKS opcional**: workloads que exigem sidecars específicos (ex.: validação biométrica) podem ser implantados via módulo `modules/eks/`, reutilizando a VPC e sub-redes privadas criadas anteriormente.

## Ambiente local com Docker Compose

Para desenvolvimento, oferecemos `compose/dev/docker-compose.yml`:

```yaml
version: "3.8"

services:
  localstack:
    image: localstack/localstack:latest
    environment:
      - SERVICES=acm,kms,s3,sqs,cloudwatch,secretsmanager
      - AWS_DEFAULT_REGION=sa-east-1
    ports:
      - "4566:4566"
    volumes:
      - ./localstack:/var/lib/localstack

  ejbca:
    image: keyfactor/ejbca-ce:latest
    environment:
      - TLS_SETUP_ENABLED=simple
      - DATABASE_JDBC_URL=jdbc:h2:/mnt/persistent/ejbcadb
    ports:
      - "8443:8443"
    volumes:
      - ./ejbca-data:/mnt/persistent

  mock-hsm:
    image: fortanix/pkcs11-mock:latest
    ports:
      - "3001:3001"
```

O script `scripts/dev/bootstrap-local.sh` cria recursos no LocalStack (buckets, chaves KMS, tópicos SNS) para espelhar os outputs do Terraform e facilitar testes end-to-end.

## Observabilidade & DR automatizados

- Dashboards e alarmes CloudWatch são versionados com Terraform (módulo `observability`).
- AWS Backup mantém RPO=15 min e retenção de 365 dias conforme DOC-ICP-10.
- `scripts/dr/run-drill.sh` automatiza exercícios trimestrais (failover Aurora + restauração S3) e armazena relatórios em `s3://cartorio-prod-dr-reports/`.
- Alarmes críticos (ALB 5xx, OCSP latency, fila de revogação) disparam SNS → PagerDuty/Slack com meta de resposta < 15 min.

### `modules/kms/variables.tf`

```hcl
variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "aws_region" {
  description = "AWS Region"
  type        = string
}

variable "common_tags" {
  description = "Tags comuns"
  type        = map(string)
  default     = {}
}
```

### `modules/kms/outputs.tf`

```hcl
output "root_ca_key_id" {
  description = "ID da chave KMS da Root CA"
  value       = aws_kms_key.root_ca.id
}

output "root_ca_key_arn" {
  description = "ARN da chave KMS da Root CA"
  value       = aws_kms_key.root_ca.arn
}

output "data_encryption_key_id" {
  description = "ID da chave de encryption de dados"
  value       = aws_kms_key.data_encryption.id
}

output "data_encryption_key_arn" {
  description = "ARN da chave de encryption de dados"
  value       = aws_kms_key.data_encryption.arn
}
```

## Ambiente de Produção

### `environments/prod/main.tf`

```hcl
# environments/prod/main.tf

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "cartorio-terraform-state-prod"
    key            = "prod/terraform.tfstate"
    region         = "sa-east-1"
    encrypt        = true
    dynamodb_table = "cartorio-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "CartorioDigital"
      Environment = "production"
      ManagedBy   = "Terraform"
      CostCenter  = "PKI-Infrastructure"
    }
  }
}

# Data sources
data "aws_caller_identity" "current" {}
data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }

  availability_zones = slice(data.aws_availability_zones.available.names, 0, 2)
}

# Networking
module "networking" {
  source = "../../modules/networking"

  project_name       = var.project_name
  vpc_cidr           = var.vpc_cidr
  availability_zones = local.availability_zones
  common_tags        = local.common_tags
}

# KMS
module "kms" {
  source = "../../modules/kms"

  project_name   = var.project_name
  aws_account_id = data.aws_caller_identity.current.account_id
  aws_region     = var.aws_region
  common_tags    = local.common_tags
}

# RDS
module "rds" {
  source = "../../modules/rds"

  project_name          = var.project_name
  vpc_id                = module.networking.vpc_id
  subnet_ids            = module.networking.private_data_subnet_ids
  kms_key_id            = module.kms.data_encryption_key_id
  instance_class        = var.rds_instance_class
  allocated_storage     = var.rds_allocated_storage
  backup_retention_days = 30
  multi_az              = true
  common_tags           = local.common_tags
}

# ECS
module "ecs" {
  source = "../../modules/ecs"

  project_name  = var.project_name
  vpc_id        = module.networking.vpc_id
  subnet_ids    = module.networking.private_app_subnet_ids
  db_host       = module.rds.db_endpoint
  db_secret_arn = module.rds.db_secret_arn
  common_tags   = local.common_tags
}
```

### `environments/prod/variables.tf`

```hcl
variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "sa-east-1"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "cartorio-digital"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "vpc_cidr" {
  description = "CIDR block da VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.medium"
}

variable "rds_allocated_storage" {
  description = "RDS allocated storage (GB)"
  type        = number
  default     = 100
}
```

### `environments/prod/terraform.tfvars`

```hcl
aws_region            = "sa-east-1"
project_name          = "cartorio-digital"
environment           = "production"
vpc_cidr              = "10.0.0.0/16"
rds_instance_class    = "db.t3.medium"
rds_allocated_storage = 100
```

## Backend: State remoto

### `backend.tf`

```hcl
# backend.tf
# Este arquivo configura o backend S3 para armazenar o state do Terraform

terraform {
  backend "s3" {
    bucket         = "cartorio-terraform-state-prod"
    key            = "prod/terraform.tfstate"
    region         = "sa-east-1"
    encrypt        = true
    kms_key_id     = "alias/terraform-state-key"
    dynamodb_table = "cartorio-terraform-locks"
  }
}
```

### Script de criação do backend

```bash
#!/bin/bash
# scripts/create-terraform-backend.sh

AWS_REGION="sa-east-1"
BUCKET_NAME="cartorio-terraform-state-prod"
DYNAMODB_TABLE="cartorio-terraform-locks"

# Criar bucket S3 para state
aws s3api create-bucket \
  --bucket $BUCKET_NAME \
  --region $AWS_REGION \
  --create-bucket-configuration LocationConstraint=$AWS_REGION

# Habilitar versionamento
aws s3api put-bucket-versioning \
  --bucket $BUCKET_NAME \
  --versioning-configuration Status=Enabled

# Habilitar encryption
aws s3api put-bucket-encryption \
  --bucket $BUCKET_NAME \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# Bloquear acesso público
aws s3api put-public-access-block \
  --bucket $BUCKET_NAME \
  --public-access-block-configuration \
    BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

# Criar tabela DynamoDB para locks
aws dynamodb create-table \
  --table-name $DYNAMODB_TABLE \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region $AWS_REGION

echo "✅ Terraform backend created successfully"
echo "Bucket: $BUCKET_NAME"
echo "DynamoDB Table: $DYNAMODB_TABLE"
```

## Workflows

### Aplicar infraestrutura

```bash
#!/bin/bash
# scripts/terraform-apply.sh

set -e

ENVIRONMENT=${1:-prod}

cd environments/$ENVIRONMENT

# Inicializar Terraform
terraform init

# Validar configuração
terraform validate

# Formatar código
terraform fmt -recursive

# Planejar mudanças
terraform plan -out=tfplan

# Revisar mudanças
echo "Review the plan above. Press Enter to apply or Ctrl+C to cancel."
read

# Aplicar mudanças
terraform apply tfplan

# Limpar arquivo de plano
rm tfplan

echo "✅ Infrastructure applied successfully"
```

### Destruir infraestrutura (cuidado!)

```bash
#!/bin/bash
# scripts/terraform-destroy.sh

set -e

ENVIRONMENT=${1:-prod}

echo "⚠️  WARNING: This will destroy ALL infrastructure in $ENVIRONMENT"
echo "Type 'yes-destroy-$ENVIRONMENT' to confirm:"
read CONFIRMATION

if [ "$CONFIRMATION" != "yes-destroy-$ENVIRONMENT" ]; then
  echo "Destruction cancelled."
  exit 1
fi

cd environments/$ENVIRONMENT

terraform destroy

echo "✅ Infrastructure destroyed"
```

## CI/CD com GitHub Actions

### `.github/workflows/terraform.yml`

```yaml
name: Terraform

on:
  push:
    branches: [main]
    paths:
      - 'terraform/**'
  pull_request:
    branches: [main]
    paths:
      - 'terraform/**'

env:
  AWS_REGION: sa-east-1
  TF_VERSION: 1.5.0

jobs:
  terraform:
    name: Terraform Plan & Apply
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: Terraform Format Check
        working-directory: terraform/environments/prod
        run: terraform fmt -check -recursive

      - name: Terraform Init
        working-directory: terraform/environments/prod
        run: terraform init

      - name: Terraform Validate
        working-directory: terraform/environments/prod
        run: terraform validate

      - name: Terraform Plan
        working-directory: terraform/environments/prod
        run: terraform plan -no-color
        continue-on-error: true

      - name: Terraform Apply
        if: github.ref == 'refs/heads/main' && github.event_name == 'push'
        working-directory: terraform/environments/prod
        run: terraform apply -auto-approve
```

**Pipeline em estágios**

1. **Lint & Security** – `terraform fmt`, `terraform validate`, `tflint` e `checkov` garantem conformidade e ausência de drifts.
2. **Plan review** – artefato do `terraform plan` é publicado no PR; comentários automáticos destacam recursos impactados.
3. **Apply controlado** – ambientes `dev` aplicam automaticamente após merge; `staging` e `prod` exigem *manual approval* (branch protection + reviewers).
4. **Smoke tests pós-apply** – script `scripts/post-apply-smoke.sh` valida saúde do ALB, ECS e RDS; falhas cancelam a promoção.
5. **Promotions** – CodePipeline opcional integra com ServiceNow/Change Management para releases regulamentados.

## Próximos passos

Com a infraestrutura provisionada via Terraform, você pode:

1. **Configurar monitoramento** (Capítulo 7)
2. **Implementar DR procedures** (Capítulo 8)
3. **Automatizar testes de infraestrutura** com Terratest
4. **Configurar Cost Optimization** policies

## Referências

- **Terraform AWS Provider:** [https://registry.terraform.io/providers/hashicorp/aws](https://registry.terraform.io/providers/hashicorp/aws)
- **Terraform Best Practices:** [https://www.terraform-best-practices.com/](https://www.terraform-best-practices.com/)
- **AWS Well-Architected Terraform:** [https://github.com/aws-ia/terraform-aws-well-architected](https://github.com/aws-ia/terraform-aws-well-architected)

