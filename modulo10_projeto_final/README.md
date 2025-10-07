# Módulo 10 – Projeto Final Integrador

Chegou a hora de consolidar todos os conhecimentos adquiridos e entregar um **cartório digital funcional**. Este módulo reúne todos os componentes desenvolvidos ao longo do curso em um único projeto integrado, pronto para demonstração e validação.

## Como navegar pelo projeto final

Este módulo foi organizado como uma jornada guiada que conecta os aprendizados dos módulos 1 a 9 e mostra como cada artefato converge para a entrega final. Recomendamos a leitura sequencial dos capítulos abaixo, sempre revisitanto os módulos anteriores quando surgirem dúvidas ou oportunidades de aprofundamento.

### Sumário dos capítulos

1. [Visão Integrada do Cartório Digital](01_visao_integrada.md) – alinhe a narrativa do problema aos blocos técnicos e jurídicos que sustentam a plataforma.
2. [Trilhas de Entrega do Cartório Digital](02_trilhas_entrega_cartorio.md) – planeje e execute o deploy final com confiança e rastreabilidade.
3. [Governança e Operação Contínua](03_governanca_operacao.md) – mantenha a conformidade viva por meio de observabilidade e auditorias contínuas.
4. [Orquestração Tecnológica do Cartório Digital](04_orquestracao_tecnologia.md) – conecte automação, segurança e infraestrutura em um fluxo único.
5. [Apresentação Executiva e Handover](05_apresentacao_execucao.md) – prepare a narrativa de valor e organize a transição para a operação.

Cada capítulo apresenta o problema real enfrentado pelo cartório digital, relembra os módulos anteriores que apoiam a solução e lista ferramentas e comandos recomendados. Juntos, eles formam uma trilha integrada: da visão estratégica que conecta os módulos 1 a 3, passando pela cadência de entrega apoiada pelos módulos 4 a 8, até chegar à governança e apresentação final potencializadas pelo módulo 9. Use-os como roteiro para montar sua demonstração final, produzir as evidências de conformidade e construir um handover inspirador.

## Objetivos

- Construir uma PKI interna robusta com CA raiz e intermediária;
- Implantar a aplicação do cartório digital com TLS 1.3, mTLS, OCSP stapling e HSTS;
- Emitir e renovar automaticamente certificados via ACME ou ACM PCA;
- Assinar certidões e artefatos gerados pela aplicação usando assinatura avançada ou qualificada, com carimbo do tempo;
- Implementar pagamentos eletrônicos e integração com o sistema SERP;
- Configurar monitoração, logs de transparência e alertas de expiração.

## Entrega final

Para concluir o curso, entregue:

1. O código fonte completo do cartório digital, incluindo os módulos anteriores integrados;
2. Scripts de implantação (Terraform/CloudFormation) e pipelines de CI/CD;
3. Um relatório descrevendo como cada requisito legal foi atendido (ICP-Brasil, ETSI, eIDAS) e quais adaptações seriam necessárias para outras jurisdições;
4. Um guia de usuário demonstrando as principais funcionalidades do sistema e o fluxo de autenticação, solicitação de certidões, assinatura de documentos e pagamento.

## Referências recomendadas

Revise todas as referências dos módulos anteriores. Consulte também exemplos de projetos open source de cartório digital ou sistemas de registro público para inspiração.
