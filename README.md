# Cartório Digital – Projeto e Curso de Certificação Digital

## Sumário

- [Prefácio: a missão do Cartório Digital](#prefácio-a-missão-do-cartório-digital)
- [Visão geral da jornada](#visão-geral-da-jornada)
- [Mapeamento dos módulos](#mapeamento-dos-módulos)
  - [Módulo 1 – Fundamentos Criptográficos](modulo1_fundamentos/README.md)
  - [Módulo 2 – PKI e Certificados X.509](modulo2_pkicertificados/README.md)
  - [Módulo 3 – TLS 1.3 e mTLS](modulo3_tls_mtls/README.md)
  - [Módulo 4 – Automação com ACME](modulo4_automacao/README.md)
  - [Módulo 5 – Conformidade Regulatória](modulo5_regulatorio/README.md)
  - [Módulo 6 – Proteção de Chaves, KMS e HSM](modulo6_kms_hsm/README.md)
  - [Módulo 7 – Assinatura de Artefatos Digitais](modulo7_assinatura_artefatos/README.md)
  - [Módulo 8 – Cloud e CI/CD do Cartório](modulo8_cloud_cicd/README.md)
  - [Módulo 9 – Observabilidade e Transparência](modulo9_observabilidade/README.md)
  - [Módulo 10 – Projeto Final Integrador](modulo10_projeto_final/README.md)
- [Como navegar pelo repositório](#como-navegar-pelo-repositório)

## Prefácio: a missão do Cartório Digital

O Brasil convive diariamente com filas em cartórios, fluxos de autenticação pouco transparentes e processos que não acompanham a velocidade do mundo digital. O **projeto Cartório Digital** nasceu para transformar esse cenário com uma proposta direta: **vamos construir o cartório enquanto aprendemos os conceitos avançados de certificação digital, assinatura, observabilidade e governança**. A cada capítulo, você une teoria e prática para entregar certidões eletrônicas com segurança, rastreabilidade e aderência às normas nacionais e internacionais.

Este repositório é o ponto de partida da jornada. Nele, você encontrará o plano completo do curso, casos reais inspirados na rotina de um cartório e os artefatos que dão vida à plataforma: infraestrutura de chaves, automação de certificados, integrações com serviços governamentais e guias de operação contínua. Cada módulo foi escrito para que desenvolvedores, arquitetos e engenheiros de segurança avancem no conhecimento enquanto evoluem o protótipo funcional do cartório.

## Visão geral da jornada

Ao longo de dez módulos, você vai percorrer desde os fundamentos de criptografia até a orquestração do ambiente em nuvem. Cada etapa começa com um desafio real do cartório digital e termina com uma entrega concreta do protótipo: enquanto aprendemos, já deixamos algo funcionando para a população e para os times internos. O resultado esperado é uma equipe capaz de:

- **Dominar os pilares de criptografia aplicada** para garantir autenticidade, sigilo e integridade de documentos.
- **Operar uma PKI completa**, emitindo, revogando e auditando certificados alinhados à ICP-Brasil e a padrões internacionais.
- **Implantar uma infraestrutura resiliente** com TLS 1.3, mTLS, automação ACME e proteção de chaves em KMS/HSM.
- **Entregar serviços contínuos em cloud**, com pipelines de CI/CD, observabilidade de ponta a ponta e governança operacional.
- **Apresentar um projeto final integrador** que conecta todas as decisões técnicas à experiência de cidadãos, escreventes e órgãos reguladores.

## Mapeamento dos módulos

Cada módulo possui um diretório próprio com um **README.md** contendo objetivos de aprendizagem, referências recomendadas e instruções passo a passo. Explore os capítulos conforme a evolução desejada:

| Módulo | Foco principal | Entrega para o projeto do cartório |
|-------:|----------------|------------------------------------|
| **1**  | Fundamentos Criptográficos | Scripts para gerar chaves, hashes e HMACs que sustentam as primeiras validações do acervo digital. |
| **2**  | PKI e Certificados X.509 | Construção da cadeia de certificação interna, emissão de certificados para serviços e simulação de revogação. |
| **3**  | TLS 1.3 e mTLS | Configuração de canais seguros e autenticação mútua entre APIs e portais do cartório. |
| **4**  | Automação (ACME/Let’s Encrypt) | Fluxos de emissão e renovação automática garantindo que nenhum serviço expire em produção. |
| **5**  | Conformidade Regulatória | Adaptação do projeto às exigências ICP-Brasil, ETSI e eIDAS, com checklists e evidências. |
| **6**  | Proteção de Chaves, KMS e HSM | Integração com AWS KMS/CloudHSM e processos de rotação manual para salvaguardar segredos críticos. |
| **7**  | Assinaturas Digitais de Artefatos | Assinatura de PDFs, aplicações e pacotes com carimbo do tempo para garantir autenticidade aos cidadãos. |
| **8**  | Cloud e CI/CD | Pipelines que levam o cartório da homologação à produção com infraestrutura como código e guardrails. |
| **9**  | Observabilidade e Transparência | Monitoramento ativo de certificados, publicação em CT logs e alertas orientados à experiência do usuário. |
| **10** | Projeto Final Integrador | Entrega do cartório digital completo, pronto para auditorias e para operar com o SERP. |

Os links diretos para os módulos estão disponíveis no [sumário](#sumário).

## Como navegar pelo repositório

1. **Clone** este repositório ou baixe-o como ZIP para trabalhar localmente com os laboratórios.
2. Acesse o diretório do módulo desejado e leia o `README.md` correspondente antes de executar qualquer comando.
3. Siga as instruções práticas para evoluir simultaneamente o seu conhecimento e o protótipo do cartório digital.
4. Registre aprendizados, personalize scripts e compartilhe melhorias – o curso foi pensado para ser iterativo e colaborativo.

Lembre-se: cada capítulo começa com um problema real do cartório digital e apresenta a ferramenta adequada como solução. Use essa narrativa para inspirar o time e acelerar a modernização do serviço notarial.
