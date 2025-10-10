# Evidência – Certificate Issuance API (Fase 3)

## Artefatos
- Serviço de aplicação para pedidos de emissão, cobrindo fila, assinatura, falha e revogação (`backend/services/issuance/src/main/java/br/com/cartoriodigital/issuance/application/CertificateOrderService.java`).
- Persistência Panache (`backend/services/issuance/src/main/java/br/com/cartoriodigital/issuance/infrastructure/persistence/PanacheCertificateOrderRepository.java`).
- API REST `/v1/certificate-orders` com operações de ciclo completo e resposta auditável (`backend/services/issuance/src/main/java/br/com/cartoriodigital/issuance/infrastructure/rest/CertificateOrderResource.java`).
- Mapeadores de erro dedicados (`backend/services/issuance/src/main/java/br/com/cartoriodigital/issuance/infrastructure/rest/error`).
- Testes unitários de regras críticas (`backend/services/issuance/src/test/java/br/com/cartoriodigital/issuance/application/CertificateOrderServiceTest.java`).

## Validação
- Execução planejada: `mvn -f backend/pom.xml test` – bloqueada pela ausência de Maven no ambiente (mesma restrição registrada anteriormente).

## Observações
- Transições obedecem ao modelo do Capítulo 3 (REQUESTED ➜ QUEUED ➜ SIGNING ➜ SIGNED) com auditoria de eventos.
- Falhas e revogações registram motivo, garantindo rastreabilidade conforme DOC-ICP-08/15.
