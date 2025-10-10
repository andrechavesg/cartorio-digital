# Serviço Validation

Descrição de alto nível do microserviço validation. Consulte docs/architecture para diagramas, contratos de API e fluxos de mensagens.

## Responsabilidades

- Definições de domínio e agregados conforme DDD.
- Endpoints expostos via OpenAPI.
- Integração com mensageria e repositórios designados.
- Controles de segurança e auditoria exigidos pela ICP-Brasil.

## Próximas ações

1. Implementar camada de aplicação (Quarkus + Kotlin onde necessário).
2. Configurar mapeamentos JPA/Reactive SQL para Aurora PostgreSQL.
3. Conectar com AWS KMS/CloudHSM utilizando PKCS#11 e rotinas de rotação.
4. Garantir testes unitários, integração (Testcontainers) e contratos (PACT) automatizados.

Artefatos de build serão organizados em src/, acompanhados por testes e scripts de migração em resources/db.
