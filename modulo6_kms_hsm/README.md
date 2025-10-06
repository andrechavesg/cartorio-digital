# Módulo 6 – Proteção de Chaves: KMS, HSM e Escrow

O sucesso de uma PKI depende da segurança das chaves privadas.  Neste módulo você irá conhecer os mecanismos de hardware security module (HSM) e Key Management Service (KMS) oferecidos por provedores de nuvem.  Verá também como implementar a rotação de chaves e políticas de escrow em conformidade com FIPS 140‑3.

## Objetivos de aprendizagem

- Entender os níveis de proteção FIPS 140‑3 e como eles se aplicam a HSMs físicos e virtuais;
- Integrar o AWS KMS/CloudHSM ou outro provedor de KMS para armazenar chaves de CA e assinaturas;
- Configurar políticas de rotação e *Bring‑Your‑Own‑Key* (BYOK) / *Hold‑Your‑Own‑Key* (HYOK);
- Assinar certificados e CSRs diretamente a partir de um HSM/KMS.

## Entrega prática

No contexto do cartório digital:

1. Configure um **ACM Private CA** ou `step‑ca` utilizando chaves armazenadas no AWS KMS;
2. Assine novos CSRs gerados no módulo 2 usando a chave no KMS e verifique a cadeia de confiança;
3. Implemente uma rotina de rotação de chave (por exemplo, a cada 90 dias) e reemita certificados para os serviços;
4. Documente como o cartório mantém a custódia das chaves (escrow) respeitando normas de conformidade.

## Referências recomendadas

- Documentação da [AWS Certificate Manager Private CA](https://docs.aws.amazon.com/privateca/latest/userguide/);
- [AWS CloudHSM](https://aws.amazon.com/cloudhsm/) e [AWS KMS](https://aws.amazon.com/kms/) – Guia de integração com PKI;
- NIST FIPS 140‑3 – Requisitos de segurança para módulos criptográficos.
