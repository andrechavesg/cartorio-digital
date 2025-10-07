# Módulo 7 – Assinaturas Digitais de Dados e Artefatos

Neste módulo você irá aprender a assinar digitalmente documentos, aplicativos e outros artefatos do cartório digital, garantindo autenticidade, integridade e valor legal. Também irá estudar o carimbo do tempo (timestamp) e os diferentes tipos de assinatura reconhecidos pela legislação brasileira e europeia.

## Sumário
- [00 — Sumário e Objetivos](capitulo-07-assinaturas-digitais/00-sumario.md)
- [01 — Tipos de Assinatura: Avançada vs Qualificada](capitulo-07-assinaturas-digitais/01-tipos-assinatura.md)
- [02 — PAdES: Assinando PDF + Timestamp](capitulo-07-assinaturas-digitais/02-pades.md)
- [03 — Assinando Artefatos de Software (JAR/EXE)](capitulo-07-assinaturas-digitais/03-assinatura-artefatos.md)
- [04 — Carimbo do Tempo (RFC 3161)](capitulo-07-assinaturas-digitais/04-timestamp-rfc3161.md)
- [05 — Prática Guiada e Critérios de Aceitação](capitulo-07-assinaturas-digitais/05-pratica-guiada.md)
- [06 — Validação e Relatório de Resultados](capitulo-07-assinaturas-digitais/06-validacao-relatorio.md)
- [07 — Integração no cartorio-digital (APIs & Jobs)](capitulo-07-assinaturas-digitais/07-apis-integracao.md)
- [08 — Testes Automatizados](capitulo-07-assinaturas-digitais/08-testes.md)
- [09 — Desafios](capitulo-07-assinaturas-digitais/09-desafios.md)
- [10 — Referências](capitulo-07-assinaturas-digitais/10-referencias.md)

## Objetivos de aprendizagem
- Entender e implementar assinaturas de documentos em diferentes formatos, como PDF/A (PAdES), arquivos JAR/EXE e contêineres;
- Configurar carimbo do tempo (RFC 3161) para prolongar a validade jurídica das assinaturas;
- Comparar assinatura digital qualificada e assinatura digital avançada:
  - **Assinatura qualificada**: utiliza certificado digital emitido por uma Autoridade Certificadora reconhecida (ICP‑Brasil, QTSP/eIDAS) armazenado em dispositivo criptográfico seguro (token, smartcard, HSM). Possui presunção máxima de autenticidade e equivalência à assinatura manuscrita.
  - **Assinatura avançada**: utiliza certificado digital controlado exclusivamente pelo signatário, mas não exige dispositivo certificado. Deve garantir vínculo único ao signatário, controle exclusivo da chave e detecção de qualquer alteração. Tem validade legal, mas a prova de autoria pode exigir demonstração técnica.
- Compreender quando cada tipo é exigido e as implicações práticas para o cartório digital.

## Entrega prática
- Assinar um documento PDF gerado no módulo 2 (por exemplo, uma certidão eletrônica) seguindo o padrão PAdES e incluir um carimbo do tempo de uma Autoridade de Carimbo de Tempo (TSA) configurada no módulo 2 ou via step‑ca;
- Assinar um artefato de software (JAR ou EXE) utilizando ferramentas como jarsigner (Java) ou signtool (Windows) e aplicar timestamp;
- Validar as assinaturas com softwares de verificação (por exemplo, Adobe Reader para PAdES) e registrar os resultados em um relatório;
- Documentar a diferença conceitual e prática entre as assinaturas avançada e qualificada e justificar a escolha de uma ou outra para cada tipo de documento.

## Referências recomendadas
- RFC 3161 – Protocolo de carimbo do tempo;
- ETSI EN 319 142‑1 – Padrão de assinatura PAdES (PDF Advanced Electronic Signatures);
- Lei 14.063/2020 e MP 2.200‑2/2001 – Definição de assinaturas eletrônicas no Brasil e reconhecimento de certificados ICP‑Brasil;
- Documentação de ferramentas de assinatura: jarsigner (Oracle), signtool (Microsoft) e soluções de timestamp (e.g. step‑ca).