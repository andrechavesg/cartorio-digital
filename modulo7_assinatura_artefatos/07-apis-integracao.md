# APIs de Integração para Assinatura

## Introdução

O cartório implementou um serviço REST que recebe documentos, aplica assinatura e carimbo de tempo e devolve os artefatos validados. Sistemas internos passaram a consumir a API, acelerando fluxos que antes dependiam de etapas manuais.

## Conceitos Fundamentais

- **Autenticação e mTLS:** protegem o acesso à API de assinatura.
- **Fluxo síncrono x assíncrono:** escolha conforme o tamanho dos arquivos e SLA.
- **Versionamento:** endpoints devem suportar evoluções sem quebrar integrações.
- **Monitoramento:** métricas de uso, latência e falhas orientam melhorias.

## Práticas Reais

1. Defina endpoints (`/assinar`, `/validar`, `/status`) e contratos JSON ou XML.
2. Exija mTLS e tokens de autorização para cada cliente da API.
3. Registre cada requisição em logs auditáveis, incluindo hash dos documentos.
4. Forneça SDKs ou exemplos para facilitar adoção pelos sistemas parceiros.

## Gancho para o Próximo Capítulo

Após expor a API, precisamos garantir que novas versões funcionem. No próximo capítulo exploraremos testes automatizados e suites de regressão inspirados por casos reais.
