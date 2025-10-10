# Apêndice – EJBCA-CE (Community Edition)

Este apêndice apresenta o **EJBCA-CE (EJBCA Community Edition)**, uma solução de PKI de código aberto que complementa os conceitos trabalhados ao longo dos módulos do projeto Cartório Digital. O EJBCA-CE é uma implementação de referência para construção de Autoridades Certificadoras (CA) robustas e escaláveis, oferecendo recursos profissionais para gestão completa de certificados digitais.

## Código-fonte do EJBCA-CE

O código-fonte oficial do EJBCA-CE está disponível como um **git submodule** neste projeto, no diretório:

```
apendices/ejbca-ce/ejbca-ce-source/
```

Para clonar o repositório completo incluindo o código-fonte do EJBCA-CE:

```bash
git clone --recursive https://github.com/andrechavesg/cartorio-digital
```

Ou, se já clonou o repositório, inicialize os submodules:

```bash
git submodule update --init --recursive
```

**Repositório oficial:** [https://github.com/Keyfactor/ejbca-ce](https://github.com/Keyfactor/ejbca-ce)

> **Nota:** Este submodule aponta para o repositório oficial do EJBCA-CE mantido pela Keyfactor, permitindo acesso direto ao código-fonte para estudo, experimentação e referência técnica.

## Sumário dos capítulos

1. [O que é o EJBCA-CE](01_introducao.md) – Visão geral, histórico e licenciamento.
2. [Arquitetura e componentes](02_arquitetura.md) – CA, RA, VA e como se integram.
3. [Instalação e configuração](03_instalacao.md) – Guia prático com Docker e configurações iniciais.
4. [Emissão e gestão de certificados](04_gestao_certificados.md) – Fluxos de CSR, emissão, renovação e revogação.
5. [Protocolos e integrações](05_protocolos.md) – ACME, OCSP, CRL e APIs REST.
6. [Comparação com AWS Private CA](06_comparacao.md) – Quando usar EJBCA-CE vs. soluções gerenciadas.
7. [Casos de uso no Cartório Digital](07_casos_uso.md) – Como aplicar EJBCA-CE no projeto.

## O que é o EJBCA-CE

O **EJBCA Community Edition** é uma solução de Infraestrutura de Chave Pública (PKI) de código aberto desenvolvida em Java pela Keyfactor. É uma das implementações de CA mais maduras do mercado, usada por organizações governamentais, empresas e instituições acadêmicas ao redor do mundo.

### Principais características

- **Autoridade Certificadora (CA):** Gerencia todo o ciclo de vida de certificados digitais – emissão, renovação, revogação e validação.
- **Autoridade de Registro (RA):** Facilita o registro e validação de identidades antes da emissão de certificados, permitindo workflows de aprovação.
- **Autoridade de Validação (VA):** Oferece serviços de validação de status de certificados via OCSP (Online Certificate Status Protocol) e publicação de CRL (Certificate Revocation List).

### Protocolos e padrões suportados

O EJBCA-CE suporta uma ampla gama de protocolos de inscrição e interfaces de integração:

- **ACME** (Automated Certificate Management Environment) – para automação de emissão e renovação
- **EST** (Enrollment over Secure Transport) – protocolo para dispositivos IoT
- **SCEP** (Simple Certificate Enrollment Protocol) – para dispositivos de rede
- **CMP** (Certificate Management Protocol) – padrão IETF para gestão de certificados
- **Web Services REST/SOAP** – para integração programática
- **Interfaces de linha de comando** – para automação e scripting

### Algoritmos criptográficos

O EJBCA-CE oferece suporte completo para algoritmos modernos e clássicos:

- **RSA** (2048, 3072, 4096 bits)
- **ECDSA** (P-256, P-384, P-521)
- **EdDSA** (Ed25519, Ed448)
- **DSA** (legado)
- Funções hash: **SHA-256**, **SHA-384**, **SHA-512**

### Perfis de certificado flexíveis

O EJBCA-CE permite criar perfis personalizados de certificados para diferentes casos de uso:

- Certificados de servidor TLS/SSL
- Certificados de cliente para autenticação mTLS
- Certificados de assinatura de código
- Certificados qualificados (QC) conforme eIDAS/ICP-Brasil
- Certificados para dispositivos IoT
- Certificados para timestamping (RFC 3161)

## Licenciamento e suporte

O EJBCA-CE é licenciado sob a **GNU Lesser General Public License (LGPL) v2.1+**, o que significa:

- ✅ **Uso livre** para fins comerciais e não comerciais
- ✅ **Modificação** do código-fonte permitida
- ✅ **Distribuição** permitida
- ⚠️ **Sem garantias** de suporte formal
- ⚠️ **Mantido pela comunidade** – atualizações seguem o ritmo da comunidade open source

Para organizações que necessitam de:
- Suporte comercial 24/7
- SLA garantido
- Funcionalidades empresariais adicionais
- Certificação Common Criteria

Existe a versão **EJBCA Enterprise Edition (EJBCA-EE)**, oferecida pela Keyfactor com licenciamento comercial.

## Arquitetura de referência

```
┌───────────────────────────────────────────────────────┐
│                     EJBCA-CE                          │
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Root CA    │  │  Issuing CA  │  │  Issuing CA  │ │
│  │   (Offline)  │──│  (TLS Certs) │  │ (Code Sign)  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                       │
│  ┌─────────────────────────────────────────────────┐  │
│  │           Registration Authority (RA)           │  │
│  │        (Aprovação de CSRs, Workflows)           │  │
│  └─────────────────────────────────────────────────┘  │
│                                                       │
│  ┌─────────────────────────────────────────────────┐  │
│  │         Validation Authority (VA)               │  │
│  │           (OCSP Responder, CRL)                 │  │
│  └─────────────────────────────────────────────────┘  │
│                                                       │
│  ┌─────────────────────────────────────────────────┐  │
│  │              Protocol Adapters                  │  │
│  │      ACME | EST | SCEP | CMP | REST API         │  │
│  └─────────────────────────────────────────────────┘  │
│                                                       │
└───────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
    Aplicações            Dispositivos         Cartório
    Web/Mobile            IoT/Edge             Digital
```

## Aplicações no Cartório Digital

O EJBCA-CE pode ser usado no projeto Cartório Digital para:

1. **CA Interna de Desenvolvimento/Testes**
   - Criar uma hierarquia completa de CA para ambientes não-produtivos
   - Testar fluxos de emissão antes de integrar com ICP-Brasil

2. **PKI para serviços internos**
   - Emitir certificados para serviços internos de backend
   - Gerenciar certificados de cliente para autenticação mTLS entre microsserviços

3. **Prova de Conceito (PoC)**
   - Demonstrar capacidades de PKI completa antes de investimento em infraestrutura comercial
   - Validar requisitos de conformidade regulatória

4. **Aprendizado e treinamento**
   - Entender o funcionamento interno de uma CA
   - Experimentar com diferentes perfis de certificado
   - Estudar protocolos ACME, OCSP, CRL na prática

5. **Backup e Disaster Recovery**
   - Manter uma CA secundária para cenários de continuidade de negócio
   - Servir como fallback em caso de indisponibilidade de provedores externos

## Integração com AWS KMS

- **Proteção de chaves privadas:** O EJBCA suporta `AWSKMSCryptoToken`, permitindo que operações de assinatura usem chaves armazenadas no AWS Key Management Service enquanto o EJBCA gerencia apenas fluxos e políticas. As chaves nunca saem do HSM gerenciado pela AWS.
- **Disponibilidade na edição certa:** O código da Community Edition referencia o token AWS KMS (por exemplo, `modules/ejbca-ejb-cli/src/org/ejbca/ui/cli/cryptotoken/CryptoTokenCreateCommand.java` e `modules/admin-gui/src/org/ejbca/ui/web/admin/cryptotoken/CryptoTokenMBean.java`), mas a classe concreta é distribuída com a edição Enterprise. Em implantações CE é necessário adicionar manualmente o provider correspondente para habilitar o recurso.
- **Provisionamento típico:** Criar o crypto token via CLI (`./bin/ejbca.sh cryptotoken create --type AWSKMSCryptoToken --awskmsregion us-east-1 ...`), ativá-lo e associá-lo ao CA Token. Utilizar credenciais ou perfis de IAM/STS (`AwsKmsAuthenticationType`) para autenticar chamadas ao KMS.
- **Persistência de certificados:** Apenas operações de chave usam KMS; metadados de certificados, CRLs e auditoria continuam armazenados no banco do EJBCA. Para arquivar certificados em serviços AWS adicionais (por exemplo, S3), configure publishers ou integrações complementares.
- **Próximos passos para produção:** Para um guia completo de hardening, automação e conformidade usando serviços AWS (KMS, Private CA, Secrets Manager, observabilidade, DR), consulte o apêndice dedicado em `apendices/aws-kms-production/README.md`.

## Diferenças entre EJBCA-CE e soluções comerciais

| Aspecto | EJBCA-CE | AWS Private CA | ICP-Brasil TSP |
|---------|----------|----------------|----------------|
| **Custo inicial** | Gratuito | Pay-per-certificate | Alto (conformidade) |
| **Hospedagem** | Self-hosted | Managed service | Regulado |
| **Escalabilidade** | Manual | Automática | Contratual |
| **Suporte** | Comunidade | 24/7 comercial | Contratual |
| **Conformidade** | DIY | SOC/ISO/PCI | ICP-Brasil |
| **Complexidade** | Alta | Baixa | Média |
| **Flexibilidade** | Máxima | Média | Baixa |

## Quando usar EJBCA-CE

✅ **Use EJBCA-CE quando:**
- Você precisa de controle total sobre a infraestrutura de PKI
- Está construindo um ambiente de desenvolvimento/testes
- Quer aprender sobre PKI na prática
- Tem requisitos de personalização que soluções gerenciadas não atendem
- Precisa suportar protocolos legados (SCEP, CMP)
- Quer evitar custos recorrentes de certificados em alta escala

❌ **Não use EJBCA-CE quando:**
- Você precisa de validade jurídica imediata (use ICP-Brasil)
- Não tem equipe para manter a infraestrutura
- Precisa de SLA garantido e suporte 24/7
- Quer time-to-market rápido (prefira soluções gerenciadas)
- Está em produção crítica sem equipe de segurança dedicada

## Integração com módulos do curso

O EJBCA-CE reforça os conceitos vistos em:

- **Módulo 2 (PKI e Certificados):** Implementação prática de hierarquia de CA
- **Módulo 4 (Automação ACME):** O EJBCA-CE possui servidor ACME integrado
- **Módulo 5 (Conformidade):** Perfis de certificado podem ser configurados para ICP-Brasil/eIDAS
- **Módulo 6 (KMS/HSM):** Suporte a integração com HSM para proteção de chaves raiz
- **Módulo 9 (Observabilidade):** APIs para monitoramento e auditoria de certificados

## Próximos passos

1. Leia os capítulos detalhados deste apêndice para entender arquitetura e operação
2. Siga o guia de instalação com Docker para ter um ambiente funcional
3. Experimente emitir certificados usando diferentes protocolos (Web UI, ACME, REST API)
4. Compare a experiência com AWS Private CA (módulo 2) e Let's Encrypt (módulo 4)
5. Decida qual abordagem faz mais sentido para cada caso de uso do Cartório Digital

## Referências

- **Site oficial:** [https://www.ejbca.org](https://www.ejbca.org)
- **Repositório GitHub:** [https://github.com/Keyfactor/ejbca-ce](https://github.com/Keyfactor/ejbca-ce)
- **Documentação:** [https://doc.primekey.com/ejbca](https://doc.primekey.com/ejbca)
- **Docker Hub:** [https://hub.docker.com/r/keyfactor/ejbca-ce](https://hub.docker.com/r/keyfactor/ejbca-ce)
- **Fórum da comunidade:** [https://github.com/Keyfactor/ejbca-ce/discussions](https://github.com/Keyfactor/ejbca-ce/discussions)

---

**Nota:** Este apêndice é complementar ao curso e não substitui a documentação oficial do EJBCA-CE. Use-o como ponto de partida para explorar PKI open source no contexto do Cartório Digital.

