# Módulo 7 – Assinaturas Digitais de Dados e Artefatos

Neste módulo você irá aprender a assinar digitalmente documentos, aplicativos e outros artefatos do cartório digital, garantindo autenticidade, integridade e valor legal. Também irá estudar o carimbo do tempo (timestamp) e os diferentes tipos de assinatura reconhecidos pela legislação brasileira e europeia.

## Sumário
- [00 — Sumário e Objetivos](00-sumario.md) – Enquadre as ameaças de falsificação de registros e mapeie quais capítulos blindam cada ponto crítico do cartório digital.
- [01 — Tipos de Assinatura: Avançada vs Qualificada](01-tipos-assinatura.md) – Compare os níveis de proteção exigidos para atos notariais e identifique quando cada assinatura evita fraudes em processos eletrônicos.
- [02 — PAdES: Assinando PDF + Timestamp](02-pades.md) – Aprenda a reforçar certidões eletrônicas com assinatura PAdES e carimbo do tempo para manter a cadeia de custódia inviolada.
- [03 — Assinando Artefatos de Software (JAR/EXE)](03-assinatura-artefatos.md) – Assine instaladores e aplicações do cartório para impedir distribuição de binários adulterados aos usuários.
- [04 — Carimbo do Tempo (RFC 3161)](04-timestamp-rfc3161.md) – Garanta que cada ato do cartório tenha data e hora auditáveis, protegendo contra repúdio e manipulações posteriores.
- [05 — Prática Guiada e Critérios de Aceitação](05-pratica-guiada.md) – Execute o fluxo completo de assinatura com checkpoints que comprovam segurança operacional ponta a ponta.
- [06 — Validação e Relatório de Resultados](06-validacao-relatorio.md) – Monte evidências técnicas para provar que os artefatos assinados resistem a auditorias e incidentes.
- [07 — Integração no cartório digital (APIs & Jobs)](07-apis-integracao.md) – Automatize assinaturas em pipelines e serviços garantindo rastreabilidade e segregação de chaves.
- [08 — Testes Automatizados](08-testes.md) – Crie testes que detectam regressões criptográficas antes que comprometam a confiança do cartório.
- [09 — Desafios](09-desafios.md) – Enfrente cenários que simulam ataques reais e evolua o plano de proteção de documentos e software do cartório.
- [10 — Referências](10-referencias.md) – Consulte normas e guias para embasar decisões de segurança com respaldo jurídico.

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

