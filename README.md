# Cartório Digital – Projeto e Curso de Certificação Digital

Este repositório contém o material e a estrutura de um curso completo sobre **certificados digitais, infraestrutura de chaves públicas (PKI)** e sua aplicação prática em um **sistema de cartório digital**.  A trilha foi pensada para desenvolvedores de software que precisam dominar os principais conceitos e ferramentas de criptografia, certificados digitais e automação de segurança.

Além de conteúdos teóricos e laboratórios de PKI, cada módulo evolui um projeto de **cartório digital** real – permitindo que o aluno aprenda enquanto constrói um sistema completo, desde a autenticação de usuários até a integração com sistemas governamentais (SERP).  No final do curso, você terá uma base sólida em criptografia aplicada e um protótipo funcional de cartório digital.

## Estrutura dos Módulos

| Módulo | Tema principal                              | Entrega prática e evolução do projeto |
|-------:|---------------------------------------------|----------------------------------------|
| **1**  | **Fundamentos Criptográficos**               | Scripts para gerar chaves, hashes e HMACs. Base teórica para uso seguro de criptografia em certificados. |
| **2**  | **PKI e Certificados X.509**                 | Construção de uma CA interna, emissão de certificados e simulação de revogação. |
| **3**  | **TLS 1.3 e mTLS**                           | Configuração de servidores seguros e autenticação mùtua entre serviços do cartório. |
| **4**  | **Automação (ACME/Let’s Encrypt)**           | Implementação de emissão e renovação automática de certificados para serviços web. |
| **5**  | **Normas e Regulamentações (ICP‑Brasil, ETSI, eIDAS)** | Análise de requisitos legais e adaptação do projeto a diferentes legislações. |
| **6**  | **Proteção de Chaves e HSM/KMS**             | Integração com AWS KMS/CloudHSM para proteger chaves privadas e emitir certificados. |
| **7**  | **Assinaturas Digitais de Artefatos**        | Assinar PDFs/JARs/EXEs com carimbo do tempo. Comparação entre assinaturas avançada e qualificada. |
| **8**  | **Implantação em Cloud e CI/CD**             | Integração do cartório digital com pipelines de entrega contínua e infraestrutura AWS. |
| **9**  | **Observabilidade e Logs de Transparência**   | Monitoramento de expiração, OCSP stapling e publicação em logs públicos de transparência. |
| **10** | **Projeto Final Integrador**                 | Entrega do cartório digital completo, com autenticação, emissão de certidões, assinaturas digitais, pagamentos e integração ao SERP. |

Cada módulo possui um diretório próprio com um **README.md** contendo os objetivos de aprendizagem, referências recomendadas e instruções passo a passo para as atividades práticas. Consulte o README de cada módulo para orientações detalhadas.

## Como usar este repositório

1. **Clone** este repositório ou baixe-o como ZIP.
2. Acesse o diretório do módulo em que deseja trabalhar e leia o `README.md` correspondente.
3. Execute os scripts e siga as instruções práticas para evoluir tanto o seu conhecimento quanto o protótipo do cartório digital.
4. Ao final de cada módulo, commit suas alterações e avance para o próximo.

Sinta‑se à vontade para adaptar o código, adicionar anotações e personalizar o projeto de acordo com as necessidades do seu time.
