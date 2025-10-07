# ICP‑Brasil e legislação brasileira

A Infraestrutura de Chaves Públicas Brasileira (ICP‑Brasil) foi instituída pela Medida Provisória n.º 2.200-2/2001 para garantir a autenticidade, integridade e validade jurídica de documentos em formato eletrônico. No contexto do cartório digital, seguir as diretrizes da ICP‑Brasil é obrigatório para que os atos tenham valor legal.

## Leis e decretos

- **MP 2.200‑2/2001** – Estabelece a ICP‑Brasil e cria o Instituto Nacional de Tecnologia da Informação (ITI) como autoridade gestora. Define que documentos assinados com certificados emitidos pela ICP‑Brasil têm presunção de validade jurídica.
- **Lei 14.063/2020** – Regula o uso de assinaturas eletrônicas na administração pública e em interações com particulares. Define níveis de assinatura (simples, avançada e qualificada) e exige que assinaturas qualificadas utilizem certificado ICP‑Brasil ou equivalentes.
- **Lei 14.382/2022** – Altera normas dos registros públicos e cria o Sistema Eletrônico dos Registros Públicos (Serp). Determina que cartórios aceitem documentos e certificados em formato eletrônico.

Outros instrumentos normativos incluem a Resolução nº 134/2022 do CNJ, que regulamenta o atendimento remoto nos cartórios, e os documentos de política de certificação (DOC‑ICP‑05, DOC‑ICP‑03, DOC‑ICP‑15 etc.) publicados pelo ITI.

## Tipos de certificado

No âmbito da ICP‑Brasil existem diferentes classes de certificados com usos específicos:

| Classe | Uso | Mídia |
|---|---|---|
| A1 | Assinatura digital de documentos e e‑mails; validade de 1 ano; chave gerada em software. |
| A3 | Assinatura digital e autenticação; validade de 1 a 3 anos; chave gerada em token ou smartcard (dispositivo criptográfico). |
| A5 | Certificados de autoridade certificadora (CA) raiz e intermediárias; utilizados para assinar outros certificados. |

Além disso, há categorias específicas para pessoas físicas (PF), pessoas jurídicas (PJ) e equipamentos (servidores, aplicações). Os certificados do tipo “S” são destinados a sigilo (criptação) e os “T” ao carimbo do tempo.

## Exigências práticas para o projeto

- **Política de certificação**: O cartório digital deve observar a DPC da AC emissora (ex.: DOC‑ICP‑05) para saber quais práticas de segurança e controle devem ser seguidas (proteção de chave, revogação, etc.).
- **Emissão e armazenamento**: Certificados de oficiais de registro e tabeliães devem ser emitidos em hardware criptográfico (token ou smartcard) quando exigido (certificado A3). O sistema deve integrar-se ao dispositivo via drivers.
- **Validação**: Para verificar um certificado ICP‑Brasil, utilize ferramentas como `openssl x509` ou ferramentas do ITI, verificando se a cadeia está ancorada na AC Raiz Brasileira.
- **Revogação**: Consulte CRLs ou servidores OCSP publicados pelas ACs para verificar se o certificado está revogado. O projeto deve automatizar essas checagens.

### Atividades

1. Acesse o site do [Portal ITI](https://www.gov.br/iti/pt-br) e baixe o DOC‑ICP‑05 e o DOC‑ICP‑15. Identifique três requisitos que impactam o desenvolvimento de um cartório digital.
2. Utilize `openssl x509 -in certificado.icp.pem -text -noout` para inspecionar um certificado ICP‑Brasil que você possua (ou utilize o repositório de cadeia pública do ITI). Observe as extensões “policyIdentifier”, “CRL Distribution Points” e “Authority Info Access”.
