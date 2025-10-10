# Infraestrutura – AWS Landing Zone e Orquestração

Este módulo contém todo o Terraform, Helm charts, Ansible e Packer necessários para provisionar e manter os ambientes da AC.

## Terraform

- modules/networking – VPC, sub-redes, rotas, security groups e WAF.
- modules/security – GuardDuty, Config, CloudTrail, IAM least privilege.
- modules/kms_hsm – Integração com AWS CloudHSM, KMS e rotação de chaves.
- modules/datastores – Aurora PostgreSQL, DynamoDB, S3 e OpenSearch.
- modules/observability – CloudWatch, Grafana, dashboards e alarmes.
- modules/application – EKS/ECS, balanceadores, ingress mTLS.
- environments – Definições por ambiente (dev, staging, prod) com remote state e pipelines.

## Kubernetes / Helm

- charts – Helm charts para cada microserviço e portais.
- manifests – Recursos adicionais (NetworkPolicies, PodSecurityStandards, Secrets).

## Automação complementar

- ansible – Hardening de bastion, servidores auxiliares e procedimentos de key ceremony.
- packer – Imagens imutáveis para workloads específicos (bastion, jump hosts).

Siga 05_iac_terraform.md e 06_seguranca_conformidade.md para critérios de aceitação, incluindo execução de terraform validate/plan, Checkov, tfsec e inspeções manuais.
