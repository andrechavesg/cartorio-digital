# Padrões Europeus e Internacionais (eIDAS & ETSI)

Nos capítulos anteriores, você viu como montar uma infraestrutura de certificação baseada na ICP‑Brasil. Entretanto, nosso cartório digital pode ter de interagir com parceiros internacionais ou adotar boas práticas de outros mercados. Por isso, é importante compreender o arcabouço europeu de identidades eletrônicas e serviços de confiança – o **Regulamento eIDAS** – e a família de normas **ETSI EN 319**, que definem perfis e exigências para provedores de serviços de confiança. Este capítulo aprofunda esses conceitos e mostra como eles se relacionam com a ICP‑Brasil.

## Regulamento eIDAS (Reg. 910/2014)

O eIDAS (eletronic IDentification, Authentication and trust Services) é um regulamento da União Europeia que define requisitos legais para identidades eletrônicas e assinaturas digitais transfronteiriças. Seus principais pontos são:

- **Reconhecimento mútuo de identidades eletrônicas**: Estados‑Membros devem aceitar os esquemas de identidade eletrônica notificados por outros países.
- **Categorias de assinatura**:
  - *Assinatura eletrônica simples*: qualquer dado em formato eletrônico associado a um signatário (como um nome digitado).
  - *Assinatura eletrônica avançada*: exige vinculação ao signatário, controle exclusivo do meio de criação e detecção de alterações.
  - *Assinatura eletrônica qualificada*: além dos requisitos de avançada, utiliza certificado qualificado emitido por um Prestador de Serviços de Confiança (QTSP) e dispositivo qualificado (QSCD). Tem presunção legal equivalente à assinatura manuscrita.
- **Serviços de confiança**: definem regras para carimbo do tempo, selagem eletrônica, autenticação de websites, certificados de selo, etc.

> ⚠️ Embora a ICP‑Brasil tenha conceitos semelhantes, o reconhecimento de assinaturas qualificada e avançada entre jurisdições depende de acordos bilaterais e ajustes de perfil. Em um cartório digital que atenda clientes europeus, pode ser necessário utilizar certificados emitidos por QTSPs europeus para documentos eletrônicos destinados à UE.

## Normas ETSI EN 319

Para implementar eIDAS, as normas ETSI detalham requisitos técnicos e perfis de certificados. Algumas das mais relevantes são:

- **ETSI EN 319 401** – Requisitos gerais de política para provedores de serviços de confiança (TSP). Define princípios de gestão de risco, segurança física e lógica, e controles organizacionais.
- **ETSI EN 319 411 (Partes 1 e 2)** – Requisitos para TSP que emitem certificados (incluindo QTSP). A Parte 1 cobre certificados “não‑qualificados” e a Parte 2 inclui requisitos adicionais para certificados qualificados e QSCD.
- **ETSI EN 319 412 (Partes 1–5)** – Perfis de certificados de assinatura e selo. Estabelece campos obrigatórios/optativos e OIDs específicos para certificados qualificados, e define extensões como *qcStatements* e *QCP* (Qualified Certificate Policy).
- **ETSI EN 319 421** – Requisitos de política e segurança para provedores de serviços de carimbo do tempo (TSP de timestamp). Similar ao RFC 3161, mas adapta o serviço à regulação eIDAS.

Estude essas normas para entender como elas influenciam a estrutura do certificado, o processo de emissão e os mecanismos de revogação.

## Atividades

1. Visite o portal da Comissão Europeia sobre eIDAS e identifique as diferenças entre assinatura eletrônica simples, avançada e qualificada.
2. Baixe um certificado qualificado de um QTSP europeu e compare os campos e extensões (`qcStatements`, OIDs) com o certificado ICP‑Brasil que você emitiu no capítulo anterior (`openssl x509 -noout -text -in cert.pem`). Observe as políticas OID e extensões específicas.
3. Leia a norma ETSI EN 319 412‑5 e verifique quais atributos e extensões são exigidos para certificados de pessoas jurídicas. Anote semelhanças e diferenças com o documento 15.03 da ICP‑Brasil.
4. Pesquise como funcionam as listas de confiança da UE (EU Trusted Lists) e identifique como elas poderiam ser consultadas pelo seu cartório digital para validar um certificado europeu.

No próximo capítulo você irá explorar em detalhes as diferenças práticas entre **assinaturas qualificadas** e **assinaturas avançadas**, e como escolher a modalidade correta para cada tipo de documento no seu projeto.
