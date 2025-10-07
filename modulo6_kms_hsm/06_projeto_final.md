# Projeto Final do Módulo 6

O desafio final consolida todo o aprendizado em uma entrega tangível: o **módulo de custódia de chaves** do Cartório Digital.

## Objetivo

Construir um serviço automatizado capaz de criar chaves, assinar documentos, rotacionar material criptográfico e registrar trilhas de auditoria completas.

## Entregáveis obrigatórios

1. **Infraestrutura como Código** que cria:
   - Uma CMK de assinatura (`SIGN_VERIFY`) e outra de criptografia (`ENCRYPT_DECRYPT`).
   - Um *custom key store* integrado a um cluster CloudHSM (ou documento descrevendo a integração se estiver usando HSM físico).
   - Políticas de acesso separando operadores, aplicações e pipelines.
2. **Aplicação de referência** (script ou microserviço) que:
   - Recebe um documento JSON e retorna a assinatura digital usando o KMS.
   - Registra metadados da operação (ID da chave, timestamp, operador) em um banco auditável.
   - Disponibiliza endpoint para verificação da assinatura.
3. **Playbook de operação** contendo:
   - Procedimentos de rotação planejada e emergência (*break-glass*).
   - Plano de resposta a incidentes envolvendo suspeita de vazamento de chave.
   - Evidências de auditoria (consultas CloudTrail, dashboards).

## Critérios de avaliação

- **Segurança:** nenhuma chave exportada, uso consistente de políticas mínimas necessárias.
- **Automação:** criação e rotação executadas por pipeline (GitHub Actions, GitLab CI, etc.).
- **Documentação:** README do serviço descrevendo dependências, variáveis e passos para execução dos scripts.
- **Demonstração:** vídeo curto ou *screencast* (opcional) mostrando a assinatura de uma certidão com auditoria habilitada.

> Concluindo esta etapa você terá a base para suportar assinaturas qualificadas no módulo 7 e para atender às exigências regulatórias avaliadas no módulo 5.
