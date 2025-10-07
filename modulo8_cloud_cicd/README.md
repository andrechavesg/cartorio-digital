# Módulo 8 – Implantação em Cloud e CI/CD

O cartório digital precisa ser implantado com agilidade e segurança em ambientes de produção. Neste módulo você irá integrar o projeto com pipelines de integração e entrega contínua (CI/CD) e preparar a infraestrutura em nuvem (AWS, Azure ou outra) para o uso de certificados automatizados.

## Sumário da jornada

1. [Visão estratégica da jornada em nuvem](./01_visao_estrategica.md) — Expõe o problema de entregas inseguras e posiciona GitHub Actions e Terraform como guardiões do pipeline.
2. [Pipelines do cartório digital](./02_pipelines_cartorio.md) — Ensina a esteira GitHub Actions etapa por etapa, reforçando gates de segurança antes de cada comando crítico.
3. [Infraestrutura como código na nuvem](./03_iac_nuvem.md) — Demonstra como Terraform replica ambientes com controle rígido de mudanças e trilhas de auditoria.
4. [Observabilidade e alertas em pipelines cloud](./04_observabilidade_alertas.md) — Conecta métricas e alertas para proteger certificados e detectar falhas antes do cidadão.
5. [Desafio final do módulo](./05_desafio_final.md) — Provoca a integração total, orientando o desenvolvedor a validar cada risco antes da virada para produção.

Cada capítulo aprofunda a maturidade do cartório digital, guiando a equipe desde a estratégia até a operação contínua na nuvem. Ao concluir a sequência, você estará preparado para consolidar todos os aprendizados no `modulo10_projeto_final`.

## Estrutura e conexões do módulo

| Capítulo | Conteúdo | Conexões principais |
| --- | --- | --- |
| [01 · Visão estratégica da jornada em nuvem](./01_visao_estrategica.md) | Panorama inspirador da jornada para cloud e CI/CD, lembrando os aprendizados dos módulos 2 a 7. | Reforça a linha do tempo que culmina no `modulo10_projeto_final` e estabelece a cultura de automação. |
| [02 · Pipelines do cartório digital](./02_pipelines_cartorio.md) | Criação de pipelines GitHub Actions com estágios de build, segurança, assinatura e deploy. | Consome scripts de assinatura do `modulo7_assinatura_artefatos` e requisitos de conformidade do `modulo5_regulatorio`. |
| [03 · Infraestrutura como código na nuvem](./03_iac_nuvem.md) | Provisionamento de ambientes com Terraform e automação de certificados em load balancers. | Conecta-se com automações do `modulo4_automacao` e governança de chaves do `modulo6_kms_hsm`. |
| [04 · Observabilidade e alertas em pipelines cloud](./04_observabilidade_alertas.md) | Instrumentação de métricas e alertas para pipelines e certificados. | Complementa as práticas de `modulo9_observabilidade` e garante continuidade operacional. |
| [05 · Desafio final do módulo](./05_desafio_final.md) | Exercício integrador que entrega uma release completa com monitoramento e documentação. | Prepara para consolidar tudo no `modulo10_projeto_final`. |

Assim, este módulo atua como ponte entre a fundação técnica construída nos módulos anteriores e a materialização completa no projeto final.

## Objetivos de aprendizagem

- Integrar a automação de emissão/renovação de certificados nos pipelines (GitHub Actions, GitLab CI, Jenkins);
- Configurar infraestrutura como código (Terraform ou CloudFormation) para implantação de serviços TLS com certificados válidos;
- Implementar alertas de expiração de certificados via CloudWatch, Grafana ou Prometheus;
- Usar serviço de malha de serviços (Istio, Envoy) para aplicar mTLS entre microserviços em ambiente Kubernetes ou ECS.

## Entrega prática

1. Criar um pipeline de CI/CD que constrói e implanta o serviço do cartório digital, incluindo a obtenção e renovação automática de certificados via ACME ou ACM PCA;
2. Utilizar Terraform ou AWS CDK para provisionar infraestrutura (ALB, ACM, Route 53) e associar certificados;
3. Configurar alertas que disparem quando um certificado estiver a menos de 30 dias da expiração;
4. Documentar o processo de implantação e como a automação mitiga riscos de indisponibilidade.

## Referências recomendadas

- Documentação da [AWS Certificate Manager](https://docs.aws.amazon.com/acm/latest/userguide/) e [ACM PCA](https://docs.aws.amazon.com/privateca/latest/userguide/);
- Guias de configuração de pipelines em [GitHub Actions](https://docs.github.com/actions) e [GitLab CI/CD](https://docs.gitlab.com/ee/ci/);
- Exemplos de mTLS em [Istio](https://istio.io/latest/docs/tasks/security/) e [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro).
