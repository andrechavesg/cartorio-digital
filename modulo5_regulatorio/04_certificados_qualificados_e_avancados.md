# Certificados Qualificados vs Avançados

No capítulo anterior você conheceu o ecossistema europeu e sua distinção entre assinaturas avançadas e qualificadas. Este capítulo aprofunda essas diferenças e mostra como elas se traduzem em certificados digitais.

## O que é um certificado qualificado?

Um certificado qualificado é emitido por um Prestador de Serviços de Confiança Qualificado (QTSP) registrado em um país da UE e segue os requisitos de qualidade e segurança definidos pelo eIDAS e pelas normas ETSI EN 319 411-2 e EN 319 412. Ele deve ser armazenado e utilizado em um **Dispositivo de Criação de Assinatura Qualificado (QSCD)**, como um smartcard ou token criptográfico certificado. O certificado contém indicações específicas (extensões qcStatements) de que é qualificado.

Características:
- Emitido por um QTSP listado na EU Trusted List.
- Contém políticas OID específicas e a extensão `QcStatement`.
- Exige uso de hardware certificado (QSCD) para assinar.
- Confere presunção de veracidade equivalente à assinatura manuscrita na UE.

## O que é um certificado avançado (não qualificado)?

Certificados avançados (ou “não qualificados”) são emitidos por autoridades certificadoras que seguem boas práticas de segurança, mas não atendem a todos os requisitos de um QTSP. Eles podem usar armazenamento de chave em software ou HSM genérico e não possuem o selo “qualificado”. Ainda assim, permitem assinaturas eletrônicas avançadas com valor jurídico, mas a validade pode precisar ser comprovada mediante perícia.

Características:
- Emitido por um TSP que pode ou não ser QTSP.
- Não necessariamente exige QSCD; a chave pode residir em HSM ou software.
- Políticas e extensões podem variar (KeyUsage, ExtendedKeyUsage).
- Valor jurídico depende de comprovação técnica em caso de disputa.

## Comparando com a ICP‑Brasil

A ICP‑Brasil não faz distinção formal entre certificados “avançados” e “qualificados”. Todos os certificados ICP‑Brasil (A1, A3, A5) podem ser utilizados para assinaturas eletrônicas com presunção de validade jurídica, desde que observados os requisitos da Lei 14.063/2020 e MP 2.200‑2/2001. Porém, alguns paralelos podem ser traçados:

- **A1 vs A3/A5**: O certificado A1 é armazenado no software e tem validade de 1 ano; as classes A3/A5 são armazenadas em dispositivo (token ou cartão) e possuem validade maior. Isso lembra a distinção entre certificados avançados (A1) e qualificados (A3/A5 com QSCD).
- **Políticas de certificado**: A ICP define OIDs e perfis (DOC‑ICP‑03, DOC‑ICP‑05) similares às ETSI EN 319 412. Nosso projeto deve mapear as extensões corretas para interoperar.

## Como escolher no projeto

No cartório digital, a escolha entre certificado qualificado e avançado depende do valor jurídico do documento e da jurisdição:

- **Documentos com valor probatório pleno na UE** (escrituras que precisam circular no bloco europeu): use certificados qualificados emitidos por QTSPs, assinados em QSCD, para garantir presunção legal.
- **Documentos internos ou nacionais**: certificados ICP‑Brasil A3/A5 ou certificados avançados em HSM podem ser suficientes.
- **Automção e DevOps**: para assinaturas de código ou selagem de logs, certificados avançados gerenciados por HSM (como AWS KMS) são mais práticos.

## Atividades

1. Faça uma tabela comparando certificado ICP‑Brasil A1, A3 e A5 com certificados avançados e qualificados eIDAS (perfis, armazenamento, validade e uso).
2. Pesquise quais QTSPs oferecem certificados qualificados de pessoa jurídica para uso em sistemas (selos de empresa) e quais dispositivos QSCD são compatíveis.
3. Configure um repositório de teste no seu cartório digital que aceite tanto certificados ICP‑Brasil quanto um certificado qualificado europeu; implemente verificação de política (OID) para diferenciar tipos de certificado.
4. Leia a Lei 14.063/2020, art. 4º, e identifique como ela classifica assinatura eletrônica simples, avançada e qualificada no contexto brasileiro.

No próximo capítulo, vamos consolidar práticas de conformidade e auditoria para que o seu cartório digital permaneça alinhado às leis nacionais e internacionais.
