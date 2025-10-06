# Módulo 8 – Implantacao em Cloud e CI/CD

O cartório digital precisa ser implantado com agilidade e segurança em ambientes de produção.  Neste módulo você irá integrar o projeto com pipelines de integração e entrega contínua (CI/CD) e preparar a infraestrutura em nuvem (AWS, Azure ou outra) para o uso de certificados automatizados.

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
