# 06 — Validação e Relatório de Resultados

Antes de cada auditoria de confiança do **Cartório Digital**, reunimos o time para revisar as evidências que sustentam nossas assinaturas. Foi essa disciplina que nos permitiu encarar reguladores, tabeliães parceiros e cidadãos com a certeza de que cada documento digital resiste a qualquer escrutínio. Validar não é burocracia: é o momento em que celebramos o cuidado técnico que coloca o cartório na vanguarda.

## Como validar
- **PAdES**: abrir no **Adobe Reader** (ou validador compatível) e verificar a cadeia e o timestamp.
- **JAR**: `jarsigner -verify -verbose -certs arquivo.jar`
- **EXE**: `signtool verify /pa /v arquivo.exe`
- **Timestamp**: `openssl ts -reply -in response.tsr -text` e verificação contra trust store.

## Entrega
- Preencher `templates/relatorio-validacao.md` com:
  - Contexto (documento/artefato, data/hora).
  - Certificado usado (tipo: qualificada/avançada, AC/QTSP).
  - Resultado da validação (prints/logs).
  - Conclusões e lições aprendidas.