# Certificados qualificados e avançados: diferenças e aplicação

No módulo 7 você aprendeu a diferença entre assinaturas qualificada e avançada do ponto de vista conceitual. Neste capítulo vamos consolidar esses conhecimentos e relacioná‑los aos requisitos dos certificados que dão suporte a cada tipo de assinatura.

## Assinatura qualificada

- Exige **dispositivo criptográfico seguro (QSCD)** conforme eIDAS ou **token/smartcard** conforme ICP‑Brasil.
- Certificado deve ser emitido por uma **autoridade certificadora qualificada** (QTSP na UE ou AC credenciada no ITI com DPC para assinatura qualificada).
- Contém indicadores no certificado, como extensões `qcStatements` (ETSI EN 319 412-5) ou `policyIdentifier` específico na cadeia ICP‑Brasil.
- Garante presunção legal de equivalência à assinatura manuscrita e não pode ser repudiada. Em tribunais europeus, somente assinaturas qualificadas têm esse status automático.

## Assinatura avançada

- Não exige QSCD; a chave pode estar em software ou HSM.
- A identidade do signatário é assegurada por processos de verificação, mas pode ser realizada por meio de cadastro biométrico, e‑mail verificado, etc.
- Emite certificado de assinatura avançada sem o selo de “qualificado”; extensões de certificado podem incluir OIDs como “etsiQcsNonRepudiation”.
- Tem validade jurídica, mas precisa ser demonstrada em tribunal se contestada.

## Requisitos de certificado

| Elemento | Assinatura avançada | Assinatura qualificada |
|---|---|---|
| Dispositivo de chave | Software ou HSM | QSCD/Token/Smartcard |
| Autoridade emissora | AC padrão (ICPs ou PKIs corporativas) | Prestador qualificado (QTSP ou AC credenciada) |
| OID de política | OID de assinatura avançada (“id‑etsi‑qcs‑QcCompliance”) | OID de política qualificada (“id‑etsi‑qcs‑QcType”) |
| Extensão `qcStatements` | Pode incluir algumas declarações | Deve incluir indicações de conformidade com QSCD e qualificação |
| Presunção legal | Deve ser provada em caso de litígio | Presumida como assinatura manuscrita |

## Como aplicar no projeto

- Para atos notariais simples, como uma certidão eletrônica com validade nacional, pode ser suficiente uma assinatura **avançada** (ex.: PAdES‑BES com certificado A1 emitido pela AC do cartório). Verifique se a legislação local (Lei 14.382/2022) aceita.
- Para escrituras públicas, procurações e atos de alto valor econômico, **assinaturas qualificadas** são recomendadas. Isso implica que o cartório digital deve integrar‑se a uma AC credenciada e emitir certificados A3 ou QSCD para tabeliães.
- No contexto europeu, usar certificados qualificados emitidos por um QTSP garante reconhecimento automático em todos os Estados‑membros. Verifique se sua PKI atende aos requisitos da EN 319 411‑2.

### Atividades

1. Analise um certificado de assinatura qualificada (pode ser um PFX de token ou QSCD europeu) com `openssl x509 -text`. Identifique as extensões `qcStatements` e `policyIdentifier`.
2. Consulte a DPC de uma AC qualificada (QTSP) e verifique os procedimentos exigidos para emissão. Compare com a DPC de uma AC padrão.
