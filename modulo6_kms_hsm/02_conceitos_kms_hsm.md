# Conceitos de KMS e HSM

## Por que separar KMS e HSM?

- **KMS** fornece APIs gerenciadas, políticas granulares (IAM) e integrações com serviços de nuvem.
- **HSM** entrega garantias físicas e certificações (FIPS 140-2/3, ICP-Brasil) que muitas vezes são exigidas por normas.
- Ao combiná-los, obtém-se praticidade operacional sem abrir mão da segurança de hardware.

## AWS Key Management Service (KMS)

Serviço gerenciado para criação, rotação e controle de uso de chaves sem exposição direta ao aplicativo. Os principais conceitos são:

### Tipos de chaves

- **CMK (Customer Managed Key):** criada e controlada pelo time do cartório; suporta rotação automática, tags e políticas customizadas.
- **AWS Managed Key:** criada automaticamente para serviços da AWS; útil para ambientes de laboratório, mas sem controle fino.
- **Data Keys:** chaves efêmeras derivadas de uma CMK por meio de *envelope encryption*. São usadas para criptografar documentos e são descartadas após o uso.

### Políticas e permissões

- Use políticas KMS para limitar operações (sign, decrypt, generate-data-key) por função do IAM.
- Habilite *grants* temporários para jobs ou pipelines que precisem de acesso momentâneo.
- Registre justificativas de acesso sensível para fins de auditoria.

## Hardware Security Module (HSM)

Dispositivo físico dedicado à geração e proteção de chaves. Nenhuma chave privada sai do hardware, garantindo que material criptográfico não possa ser exportado em texto claro.

### Padrões relevantes

- **FIPS 140-2/3:** requisito comum para ICP-Brasil e governos.
- **ISO/IEC 19790:** requisitos de segurança para módulos criptográficos.
- **DOC-ICP-02/03:** controles técnicos para Autoridades Certificadoras brasileiras.

### Modelos de HSM

- **CloudHSM (AWS) / Key Vault HSM (Azure):** sob demanda, integrados à nuvem.
- **Appliances dedicados (Thales, Utimaco):** instalados em data centers próprios, exigem equipe especializada.
- **Dispositivos compactos (YubiHSM, Nitrokey HSM):** adequados para laboratórios ou ambientes de homologação.

## Integração KMS ↔ HSM

Em cenários regulatórios, o KMS atua como *front-end*, enquanto a geração de chaves raiz acontece dentro de um cluster HSM. Assim, operações sensíveis permanecem em hardware certificado, mas a aplicação conversa com o KMS via APIs padronizadas.
