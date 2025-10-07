# Objetivo do Módulo

Neste módulo você irá projetar e implementar a camada de **custódia de chaves** do Cartório Digital. O objetivo é dominar as
estratégias para gerar, armazenar, rotacionar e auditar material criptográfico sensível sem expor segredos na aplicação.

Ao final, o time será capaz de:

- Definir uma política de uso de chaves que diferencia certificados de produção, homologação e laboratório.
- Operar o **AWS Key Management Service (KMS)** para criar chaves mestres de assinatura, criptografia e selagem de dados.
- Integrar um **Hardware Security Module (HSM)** para cenários que exijam certificação FIPS/ICP-Brasil.
- Monitorar operações críticas e reagir a incidentes que envolvam o comprometimento de chaves.

> **Importante:** os conhecimentos aqui consolidados são requeridos para atender aos requisitos do DOC-ICP-05 (política de certificação) e para a construção do fluxo de assinatura qualificada do módulo 7.
