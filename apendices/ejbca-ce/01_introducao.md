# Capítulo 1 – Introdução ao EJBCA-CE

## O que é o EJBCA-CE

O **EJBCA Community Edition (EJBCA-CE)** é uma solução de PKI (Public Key Infrastructure) de código aberto que permite a implementação de uma Autoridade Certificadora (CA) completa e profissional. O nome EJBCA vem de **Enterprise Java Beans Certificate Authority**, refletindo sua origem como um projeto Java EE.

Desenvolvido desde 2002 pela empresa sueca PrimeKey (agora parte da Keyfactor), o EJBCA é uma das implementações de CA mais maduras e amplamente utilizadas no mundo open source. Sua versão Community Edition oferece recursos profissionais gratuitamente, permitindo que organizações de qualquer porte implementem uma PKI robusta e escalável.

## Histórico e evolução

- **2002:** Primeira versão do EJBCA lançada como projeto open source
- **2006:** Adoção por governos europeus para projetos de identidade digital
- **2010:** Certificação Common Criteria (versão Enterprise)
- **2015:** Adição de suporte a protocolos modernos (ACME, EST)
- **2018:** Containerização com Docker para facilitar implantação
- **2020:** Integração com Kubernetes e arquiteturas cloud-native
- **2023:** Suporte completo a algoritmos pós-quânticos (em desenvolvimento)

Hoje, o EJBCA é usado por:
- Governos nacionais (eIDs, passaportes eletrônicos)
- Operadoras de telecomunicações (SIM cards, eSIM)
- Empresas industriais (certificados para IoT e OT)
- Instituições financeiras (autenticação forte)
- Organizações que precisam de PKI interna

## Por que EJBCA-CE é relevante para o Cartório Digital

No contexto do **projeto Cartório Digital**, o EJBCA-CE oferece:

### 1. Ambiente de aprendizado completo

Ao invés de apenas usar APIs de terceiros (AWS Private CA, Let's Encrypt), você pode:
- Ver como uma CA funciona internamente
- Entender fluxos de aprovação e validação
- Experimentar com diferentes perfis de certificado
- Testar cenários de revogação e validação

### 2. Laboratório de testes

Durante o desenvolvimento do cartório, você pode:
- Criar certificados de teste sem custos
- Simular hierarquias complexas de CA (Root CA → Intermediate CA → End Entity)
- Testar integração com HSM antes de usar hardware real
- Validar requisitos de conformidade (ICP-Brasil, eIDAS) em ambiente controlado

### 3. PKI interna para microsserviços

O cartório digital pode usar EJBCA-CE para:
- Emitir certificados para comunicação entre serviços (mTLS)
- Gerenciar identidades de containers e pods Kubernetes
- Autenticar dispositivos IoT (ex.: totens de autoatendimento)
- Criar certificados para APIs internas que não precisam de validade pública

### 4. Prova de conceito antes de investimento

Antes de contratar um TSP (Trust Service Provider) da ICP-Brasil ou investir em infraestrutura comercial, você pode:
- Demonstrar o valor de uma PKI completa para stakeholders
- Validar requisitos técnicos e funcionais
- Estimar custos operacionais de manutenção
- Treinar a equipe em operação de PKI

### 5. Backup e resiliência

Em cenários de missão crítica, o EJBCA-CE pode servir como:
- CA secundária para continuidade de negócio
- Fallback em caso de indisponibilidade de provedores externos
- Fonte de certificados durante janelas de manutenção

## Componentes principais

O EJBCA-CE é composto por três módulos principais:

### 1. Autoridade Certificadora (CA)

Responsável por:
- Gerar e assinar certificados digitais
- Manter hierarquias de confiança (Root CA, Intermediate CA)
- Gerenciar chaves privadas (protegidas por senha ou HSM)
- Publicar certificados emitidos

**Funcionalidades:**
- Múltiplas CAs em uma única instância
- Suporte a diferentes algoritmos (RSA, ECDSA, EdDSA)
- Perfis de certificado customizáveis
- Extensões X.509 configuráveis

### 2. Autoridade de Registro (RA)

Responsável por:
- Receber e validar requisições de certificado (CSR)
- Aprovar ou rejeitar solicitações conforme políticas
- Gerenciar identidades e credenciais de usuários
- Implementar workflows de aprovação

**Funcionalidades:**
- Autenticação de solicitantes (senha, token, certificado)
- Aprovação manual ou automática
- Integração com diretórios LDAP/Active Directory
- API para integração com sistemas externos

### 3. Autoridade de Validação (VA)

Responsável por:
- Responder consultas OCSP (Online Certificate Status Protocol)
- Publicar e distribuir CRLs (Certificate Revocation Lists)
- Validar status de certificados em tempo real
- Fornecer provas de não-revogação

**Funcionalidades:**
- OCSP Responder de alta performance
- Suporte a OCSP Stapling
- Publicação automática de CRL
- Assinatura de respostas OCSP

## Arquitetura técnica

O EJBCA-CE é construído sobre a plataforma Java EE (agora Jakarta EE) e utiliza:

- **Linguagem:** Java 11+ (OpenJDK)
- **Application Server:** WildFly / JBoss EAP
- **Banco de dados:** PostgreSQL, MySQL, MariaDB, Oracle, MSSQL
- **Protocolos web:** HTTP/HTTPS (REST, SOAP)
- **Interface administrativa:** Web UI baseada em JSF

**Stack de segurança:**
- Bibliotecas criptográficas: BouncyCastle
- Suporte a HSM: PKCS#11, Azure Key Vault, AWS CloudHSM
- Autenticação: Certificados de cliente, LDAP, OAuth2

## Modelos de implantação

O EJBCA-CE pode ser implantado de várias formas:

### 1. Container Docker (recomendado para desenvolvimento)

```bash
docker run -d \
  --name ejbca \
  -p 8080:8080 \
  -p 8443:8443 \
  -e TLS_SETUP_ENABLED=simple \
  keyfactor/ejbca-ce:latest
```

**Vantagens:**
- Setup rápido (< 5 minutos)
- Isolamento de ambiente
- Fácil destruição e recriação

**Desvantagens:**
- Persistência de dados requer volumes
- Configuração avançada requer customização de imagens

### 2. Instalação tradicional em VM/bare metal

Instalação manual do WildFly, banco de dados e EJBCA WAR.

**Vantagens:**
- Controle total sobre configuração
- Performance otimizada
- Integração com HSM físico

**Desvantagens:**
- Configuração complexa
- Manutenção trabalhosa
- Dificuldade de replicação

### 3. Kubernetes (produção escalável)

Deployment com Helm charts oficiais.

**Vantagens:**
- Escalabilidade horizontal
- Alta disponibilidade
- Integração com cloud-native stack

**Desvantagens:**
- Complexidade operacional
- Requer conhecimento de Kubernetes
- Custos de infraestrutura

## Diferenças entre CE e EE

| Recurso | Community Edition (CE) | Enterprise Edition (EE) |
|---------|----------------------|------------------------|
| **Licença** | LGPL (open source) | Comercial |
| **Custo** | Gratuito | Pago (por instância) |
| **Suporte** | Comunidade (GitHub) | Suporte 24/7 SLA |
| **Funcionalidades core** | ✅ Completas | ✅ Completas + extras |
| **ACME** | ✅ | ✅ |
| **SCEP/EST/CMP** | ✅ | ✅ |
| **HSM integration** | ✅ PKCS#11 | ✅ PKCS#11 + vendors |
| **Clustering** | ❌ | ✅ |
| **Peer Connectors** | ❌ | ✅ |
| **Advanced RA** | ❌ | ✅ |
| **Common Criteria** | ❌ | ✅ |
| **Audit logging** | Básico | Avançado |

## Licenciamento LGPL – O que significa na prática

A licença **GNU Lesser General Public License v2.1+** permite:

✅ **Permitido:**
- Usar EJBCA-CE em ambiente comercial (inclusive cartórios)
- Modificar o código-fonte conforme necessário
- Distribuir versões modificadas (desde que compartilhe mudanças)
- Integrar EJBCA-CE com software proprietário via APIs

⚠️ **Obrigações:**
- Se modificar o código EJBCA-CE e distribuir, deve disponibilizar as modificações sob LGPL
- Deve manter avisos de copyright e licença
- Aplicações que apenas **usam** EJBCA-CE (via API/REST) não precisam ser open source

❌ **Não permitido:**
- Remover avisos de licença ou copyright
- Usar marca registrada "EJBCA" sem permissão
- Vender suporte comercial sem autorização da Keyfactor

**Conclusão prática:** Você pode usar EJBCA-CE no Cartório Digital sem restrições, desde que não modifique o código interno do EJBCA. Integrações via API são totalmente livres.

## Casos de uso reais

### Governo Federal da Finlândia
Usa EJBCA para emitir identidades digitais nacionais (eID) para todos os cidadãos.

### Operadora Telia
Usa EJBCA para gerenciar certificados de milhões de SIM cards e dispositivos IoT.

### Universidades europeias
Usam EJBCA para emitir certificados de acesso a redes Wi-Fi (eduroam) e VPN.

### Empresas industriais
Usam EJBCA para PKI interna de fábricas (OT – Operational Technology), emitindo certificados para PLCs, HMIs e controladores.

## Por que estudar EJBCA-CE neste curso

Ao trabalhar com EJBCA-CE, você:

1. **Aprende na prática** como funciona uma CA real (não apenas teoria)
2. **Complementa** o conhecimento adquirido nos módulos 2, 4, 5 e 6
3. **Ganha autonomia** para avaliar soluções de PKI (open source vs. comercial)
4. **Desenvolve habilidades** valorizadas no mercado de segurança digital
5. **Prepara-se** para certificações como CISSP, CEH, ou especializações em PKI

## Próximos passos

No próximo capítulo, vamos detalhar a **arquitetura e componentes** do EJBCA-CE, mostrando como CA, RA e VA trabalham juntos para formar uma PKI completa.

Em seguida, você seguirá para o **guia de instalação prática**, onde subirá uma instância EJBCA-CE com Docker e emitirá seus primeiros certificados.

## Referências

- **Site oficial:** [https://www.ejbca.org](https://www.ejbca.org)
- **Documentação técnica:** [https://doc.primekey.com/ejbca](https://doc.primekey.com/ejbca)
- **Repositório GitHub:** [https://github.com/Keyfactor/ejbca-ce](https://github.com/Keyfactor/ejbca-ce)
- **White paper:** "Building a PKI with EJBCA" – disponível no site oficial
- **RFC 5280:** Internet X.509 Public Key Infrastructure Certificate and CRL Profile

