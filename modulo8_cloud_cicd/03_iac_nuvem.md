# 03 · Infraestrutura como código na nuvem

Os administradores do cartório digital sofriam para reproduzir ambientes de homologação e produção à mão, o que atrasava liberações e fragilizava auditorias. Como resposta inspiradora, adotamos Terraform para descrever cada recurso de nuvem do projeto principal, garantindo que qualquer pessoa consiga recriar a plataforma com o mesmo rigor jurídico.

## Problema a resolver

Manter ambientes idênticos entre homologação e produção é essencial para evitar surpresas durante a emissão de certidões. Alterações manuais em consoles dificultam auditorias e podem comprometer a conformidade exigida pelos órgãos reguladores.

## Conceito: infraestrutura versionada

Encaramos a infraestrutura como parte do código-fonte. Antes de aplicar qualquer comando, avaliamos dependências com os módulos anteriores: certificados e chaves (módulos 2 e 6), automações (`modulo4_automacao`) e observabilidade (`modulo9_observabilidade`). Só depois de mapear esses relacionamentos definimos o plano Terraform.

## Escolha do Terraform

Optamos por Terraform pela sua linguagem declarativa e amplo ecossistema de provedores. Com ele, descrevemos VPCs, balanceadores, certificados e funções serverless que sustentam o cartório digital.

## Exemplo guiado

Antes de escrever código, alinhamos a arquitetura mínima:

1. **Rede segura** com VPC, sub-redes públicas para load balancers e privadas para serviços.
2. **Balanceador com HTTPS** associado a certificado emitido pela autoridade interna (módulos 2 e 6).
3. **Serviço containerizado** (ECS ou EKS) recebendo as imagens validadas no módulo de pipelines.

O trecho abaixo demonstra a criação de um Application Load Balancer protegido por TLS usando Terraform.

```hcl
# infrastructure/load_balancer.tf
resource "aws_lb" "cartorio" {
  name               = "cartorio-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = aws_subnet.public.*.id
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.cartorio.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.cartorio.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.cartorio.arn
  }
}

resource "aws_acm_certificate" "cartorio" {
  domain_name       = "api.cartorio.digital"
  validation_method = "DNS"
}
```

### Aplicando o plano

```bash
# Por que: inicializa plugins e backends garantindo consistência com o estado remoto.
terraform init
# Por que: valida a proposta de mudança para revisão entre pares.
terraform plan -out=tfplan
# Por que: aplica somente o que foi aprovado, preservando o rastro do plano.
terraform apply tfplan
```

Cada execução gera logs versionados que comprovam quem alterou o ambiente e quando. Este material é compartilhado com a equipe regulatória, reforçando os controles apresentados em `modulo5_regulatorio`.

## Integração com outros módulos

- O módulo de automação (`modulo4_automacao`) fornece scripts que disparam o `terraform apply` após aprovações.
- O módulo de observabilidade (`modulo9_observabilidade`) consome *tags* e *outputs* definidos aqui para montar dashboards de saúde da infraestrutura.

Com a infraestrutura sob controle de versão, o cartório digital conquista previsibilidade e pode ampliar sua atuação para novas regiões com confiança — um passo decisivo rumo ao `modulo10_projeto_final`.
