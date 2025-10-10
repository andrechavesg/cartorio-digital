# Plataforma CA ICP-Brasil – Implementação

Este diretório materializa o programa aws-kms-production, provendo base de código, infraestrutura como código, automações de segurança e documentação operacional para a AC em conformidade com a ICP-Brasil.

## Componentes Principais

- Backend – Microserviços para identidade, emissão, revogação, validação e auditoria.
- Frontend – Portais dedicados para operadores, agentes de registro e auditores.
- Infraestrutura – Terraform modular, Helm Charts e automações para ambientes AWS multi-AZ.
- DevSecOps – Pipelines de CI/CD, ferramentas de varredura, automações de compliance e evidências.
- Documentação – Runbooks, cerimônias de chave, políticas e arquitetura atualizada.

## Como navegar

1. Leia IMPLEMENTATION_PLAN.md para entender o backlog de implementação.
2. Consulte docs/ para artefatos normativos e operacionais.
3. Utilize scripts/ e devsecops/ para rotinas automatizadas de segurança e compliance.
4. Cada microserviço possui README próprio com instruções de build, testes e deploy.

## Próximos Passos

- Completar o plano de implementação, priorizando requisitos regulatórios.
- Integrar pipelines e provas de execução conforme descrito no PLAN.md original.
