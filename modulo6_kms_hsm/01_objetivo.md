# Objetivo do Módulo – KMS e HSM

## Exemplo Inspirador

Ao revisar os relatórios de segurança, o cartório descobriu que algumas chaves privadas ainda estavam armazenadas em servidores comuns. A equipe decidiu migrá-las para um HSM integrado a um KMS gerenciado, garantindo proteção com hardware certificado. A mudança elevou instantaneamente a confiança de todos os envolvidos.

## Conceitos Fundamentais

- **HSM (Hardware Security Module):** dispositivo físico que guarda chaves com alto nível de proteção.
- **KMS (Key Management Service):** serviço que orquestra geração, rotação e uso de chaves.
- **Integração com aplicativos:** APIs e políticas controlam quem pode usar cada chave.
- **Auditoria contínua:** logs e métricas garantem transparência e conformidade.

## Práticas Reais

1. Levante todas as chaves críticas do cartório (assinaturas, criptografia de base de dados, tokens) e classifique riscos.
2. Defina quais chaves migrarão para HSM dedicado e quais podem permanecer em KMS gerenciado.
3. Documente papéis e responsabilidades para acesso às chaves (operadores, auditores, administradores).
4. Planeje testes de desastre para validar backups e procedimentos de recuperação.

## Gancho para o Próximo Capítulo

Com o objetivo claro, vamos mergulhar nos conceitos que sustentam essa migração. No próximo capítulo veremos, por meio de um exemplo inspirador, como KMS e HSM trabalham juntos para proteger a espinha dorsal criptográfica do cartório digital.
