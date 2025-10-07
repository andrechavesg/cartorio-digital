# 05 — Prática Guiada e Critérios de Aceitação

## O que implementar
1. **PAdES**: assinar um PDF gerado no Módulo 2 (certidão) + **timestamp**.
2. **Artefato**: assinar um **JAR** (ou **EXE** em Windows) com **timestamp**.
3. **Relatório**: validar em ferramentas (Adobe Reader, `jarsigner -verify`, `signtool verify`) e preencher `templates/relatorio-validacao.md`.
4. **Doc**: registrar a diferença entre **avançada** e **qualificada** e **justificar** a escolha por tipo de documento.

## Critérios de aceitação
- PAdES assinado **com sucesso** e validado em leitor de PDF.
- Artefato (JAR/EXE) com **timestamp válido**.
- Relatório contendo **prints/logs** de validação.
- Decisão **fundamentada** sobre **avançada vs qualificada** por categoria de documento.