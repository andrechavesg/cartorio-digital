# Conceitos de KMS e HSM

## AWS Key Management Service (KMS)
Serviço gerenciado para criação, rotação e controle de uso de chaves sem exposição direta ao app.

### Tipos de chaves
- **CMK (Customer Managed Key)**: criada e controlada pelo cliente.
- **AWS Managed Key**: criada e gerenciada pela AWS.
- **Data Keys**: chaves efêmeras para criptografia de dados (envelope encryption).

## Hardware Security Module (HSM)
Dispositivo físico dedicado à geração e proteção de chaves. Nenhuma chave privada sai do hardware.

### Padrões relevantes
- FIPS 140-2/3
- ISO/IEC 19790
