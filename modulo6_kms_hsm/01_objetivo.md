# Objetivo do Módulo

Este módulo demonstra como armazenar, proteger e gerenciar **chaves criptográficas** em ambiente seguro.
O foco é garantir que operações como emissão de certificados, assinaturas digitais e carimbo do tempo
ocorram em ambiente **controlado e auditável**.

No projeto do **Cartório Digital**, vamos integrar o **AWS KMS** para custodiar as chaves mestres da aplicação
e acoplar o uso de **HSM** quando necessário para requisitos de conformidade.
