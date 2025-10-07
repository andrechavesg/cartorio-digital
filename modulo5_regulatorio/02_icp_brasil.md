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
- **Validação**: Consulte a seção a seguir para estruturar a conferência dos certificados e gerar evidências técnicas para auditorias.
- **Revogação**: Consulte CRLs ou servidores OCSP publicados pelas ACs para verificar se o certificado está revogado. O projeto deve automatizar essas checagens.

## Validação

No cartório digital, auditores internos e externos frequentemente precisam comprovar a origem de um certificado ICP‑Brasil para validar um ato registral. Esse processo demanda evidências técnicas que indiquem de qual Autoridade Certificadora o certificado foi emitido, quais políticas estão vinculadas e se a cadeia está ancorada na AC Raiz Brasileira. Os fluxos de integração e monitoramento construídos nos módulos anteriores — da modelagem de certificados (módulo 2) à verificação automatizada em pipelines (módulo 4) — dependem dessa análise para que o time jurídico aprove a operação e, consequentemente, para que a segurança jurídica do cartório digital seja preservada.

Antes de recorrer a ferramentas de inspeção, os auditores devem reconstruir a cadeia de confiança a partir das fontes oficiais da ICP‑Brasil. Isso envolve baixar do repositório do ITI a AC Raiz e as ACs intermediárias declaradas no certificado apresentado no ato, confirmar as impressões digitais publicadas nos DOC‑ICP e verificar se cada elo da cadeia consta nas listas de confiabilidade (LCR/Lista de Confiabilidade de Certificados). Somente depois dessa conferência é que a equipe valida as datas de vigência, as políticas de certificação aplicáveis e os mecanismos de revogação, registrando a evidência de que a cadeia completa está presente e íntegra.

Com a cadeia comprovada e arquivada no dossiê de auditoria, a equipe de conformidade gera relatórios técnicos extraindo os metadados do certificado do usuário final para manter a rastreabilidade do ato. Uma forma direta de obter essas informações — e que complementa a reconstrução da cadeia — é executar `openssl x509 -in certificado.icp.pem -text -noout`, que apresenta as políticas, pontos de distribuição de CRL e dados de Autoridade de Certificação. A saída do comando deve ser anexada ao relatório junto com o carimbo de tempo da análise, reforçando a confiança jurídica do documento.

### Atividades

1. Acesse o site do [Portal ITI](https://www.gov.br/iti/pt-br) e baixe o DOC‑ICP‑05 e o DOC‑ICP‑15. Identifique três requisitos que impactam o desenvolvimento de um cartório digital.
2. Reproduza a cadeia de certificação de um certificado ICP‑Brasil usado em atos do cartório digital: faça o download da AC Raiz, das ACs intermediárias e do certificado final; valide os hashes publicados pelo ITI e documente o caminho de certificação.
3. Considere a dor do projeto em garantir que as extensões obrigatórias do certificado (como políticas e pontos de revogação) estejam presentes para manter os fluxos automatizados dos módulos anteriores. Descreva quais evidências você precisa coletar e, na sequência, execute `openssl x509 -in certificado.icp.pem -text -noout` em um certificado ICP‑Brasil (próprio ou do repositório público do ITI) para verificar campos como “policyIdentifier”, “CRL Distribution Points” e “Authority Info Access”.
