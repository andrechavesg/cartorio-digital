# Módulo 2 – PKI e Certificados X.509

Dando continuidade ao módulo de fundamentos criptográficos, neste módulo você aprenderá a construir e administrar uma infraestrutura de chaves públicas (PKI). Dominar a PKI é fundamental para emitir os certificados que serão usados no cartório digital, validar identidades e revogar credenciais comprometidas. Este conhecimento prepara o terreno para o próximo módulo, onde usaremos esses certificados em conexões TLS e mTLS.

## Sumário dos capítulos

1. [Objetivo do módulo](01_objetivo.md) – Importância de uma PKI para o cartório digital e ligação com o módulo anterior.
2. [Estrutura de um certificado](02_estrutura_certificado.md) – Campos de um X.509, extensões (SAN, EKU) e comandos de inspeção.
3. [Montar uma CA](03_montar_ca.md) – Criar CA raiz e intermediária com OpenSSL ou step-ca.
4. [CSRs e emissão de certificados](04_csr_emissao_certificados.md) – Gerar CSRs e assinar certificados de servidor/cliente.
5. [Revogação e validação](05_revogacao_validacao.md) – Gerar CRL, configurar OCSP e validar cadeias de confiança.

## Objetivos de aprendizagem

- Compreender a estrutura de um certificado X.509 e seus campos (Subject, Issuer, SAN, KeyUsage);
- Construir uma Autoridade Certificadora (CA) raiz e uma CA intermediária usando `openssl` ou `step-ca`;
- Gerar requisições de assinatura de certificado (CSR) e emitir certificados de servidor e cliente;
- Simular revogação por CRL e OCSP e validar cadeias de confiança.

## Entrega prática

Implemente uma CA interna no diretório deste módulo. Emitindo certificados:

1. Crie uma **CA raiz** e uma **CA intermediária** com configurações de política próprias;
2. Gere CSRs para o servidor do cartório digital e para clientes (usuários);
3. Assine e instale os certificados, montando a cadeia de confiança;
4. Configure um serviço simples (por exemplo, um servidor Nginx) com o certificado emitido;
5. Gere uma CRL e configure um servidor OCSP para simular revogação.

## Referências recomendadas

- **RFC 5280** – Perfil de certificados X.509 e diretrizes de validação;
- **RFC 6960** – Protocolo OCSP para verificação de status de revogação;
- **ETSI EN 319 411-1** – Requisitos para provedores de serviços de confiança e perfis de certificado.
