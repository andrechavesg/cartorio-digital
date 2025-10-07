# Módulo 6 — KMS e HSM no Cartório Digital

Este módulo guia a equipe de desenvolvimento na construção de uma camada de **custódia de chaves** resiliente e auditável para o cartório digital. O foco é demonstrar como combinar recursos gerenciados de nuvem (AWS KMS) e módulos de segurança de hardware (HSM) para proteger chaves raiz da PKI, selos de tempo e segredos de aplicação.

## Como este módulo se conecta à trilha

- Dá continuidade aos módulos 2 a 4, onde já emitimos certificados e automatizamos renovações.
- Fornece a base de segurança necessária para cumprir os requisitos regulatórios do módulo 5.
- Entrega artefatos que serão reutilizados para assinatura de documentos (módulo 7) e implantação em cloud (módulo 8).

## Estrutura do módulo

1. [Objetivo do módulo](01_objetivo.md)
2. [Conceitos de KMS e HSM](02_conceitos_kms_hsm.md)
3. [Implementação de KMS no Cartório Digital](03_implementacao_kms_cartorio.md)
4. [HSM e criptografia de hardware](04_hsm_e_criptografia_hardware.md)
5. [Práticas de auditoria e rotação de chaves](05_praticas_auditoria_rotacao.md)
6. [Projeto final do módulo](06_projeto_final.md)

Cada capítulo combina fundamentos teóricos com laboratórios práticos. Ao final, você terá pipelines que criam, rotacionam e auditam chaves criptográficas críticas — um pré-requisito para operar como Autoridade de Registro ou emissor de certificados com validade jurídica.
