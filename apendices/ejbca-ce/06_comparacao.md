# Capítulo 6 – Comparação com AWS Private CA e Outras Soluções

## Visão geral das opções de PKI

Ao planejar a infraestrutura de PKI para o Cartório Digital, você tem três categorias principais de soluções:

1. **Self-hosted open source:** EJBCA-CE, OpenCA, Dogtag
2. **Managed cloud services:** AWS Private CA, Azure Key Vault, Google CAS
3. **Commercial/Enterprise:** EJBCA-EE, DigiCert PKI Platform, Entrust

## EJBCA-CE vs AWS Private CA

### Comparação detalhada

| Aspecto | EJBCA-CE | AWS Private CA |
|---------|----------|----------------|
| **Modelo de custo** | Gratuito (self-hosted) | $400/mês por CA + $0.75/cert |
| **Infraestrutura** | Você gerencia (VMs, containers) | Totalmente gerenciada |
| **Escalabilidade** | Manual (adicionar instâncias) | Automática (AWS cuida) |
| **Disponibilidade** | Você configura (HA manual) | 99.9% SLA (multi-AZ) |
| **Backup** | Você implementa | Automático (AWS) |
| **Protocolos** | ACME, SCEP, EST, CMP, REST | API AWS + ACME (limitado) |
| **UI administrativa** | Web UI completa | AWS Console (básico) |
| **Perfis de certificado** | Ilimitados e customizáveis | Templates AWS predefinidos |
| **Integração HSM** | PKCS#11 (qualquer HSM) | AWS CloudHSM apenas |
| **Auditoria** | Logs locais + export | CloudTrail nativo |
| **Tempo de setup** | 1-2 horas (Docker) | 5 minutos (Console/CLI) |
| **Operação** | Requer equipe dedicada | Mínima intervenção |
| **Curva de aprendizado** | Alta (PKI complexa) | Média (APIs AWS) |
| **Vendor lock-in** | Nenhum | Alto (AWS) |
| **On-premises** | ✅ Sim | ❌ Não (apenas cloud) |
| **Air-gapped** | ✅ Sim | ❌ Não |

### Análise de custos

#### EJBCA-CE

**Custos iniciais:**
- Software: $0 (open source)
- Infraestrutura: Variável (VMs/containers)
- Implementação: 40-80 horas de engenharia

**Custos recorrentes (exemplo com 10.000 certificados/ano):**
```
Servidor (2 vCPU, 8GB RAM, AWS EC2 t3.medium): $30/mês
Banco PostgreSQL RDS (db.t3.small): $25/mês
Backup S3 (50GB): $1/mês
Engenheiro DevOps (20% tempo): $1.500/mês
--------------------------------------------------
Total mensal: ~$1.556
Total anual: ~$18.672

Custo por certificado: $1.87
```

#### AWS Private CA

**Custos (mesmo volume – 10.000 certificados/ano):**
```
CA ativa (1x): $400/mês
Certificados emitidos (10.000): $0.75 × 10.000 = $7.500/ano
--------------------------------------------------
Total mensal: $400 + $625 = $1.025
Total anual: $4.800 + $7.500 = $12.300

Custo por certificado: $1.23
```

**Quando AWS Private CA é mais barato:**
- Volume baixo (< 5.000 certificados/ano)
- Não há equipe de PKI disponível
- Curto prazo (< 2 anos)

**Quando EJBCA-CE é mais barato:**
- Volume alto (> 20.000 certificados/ano)
- Já existe infraestrutura e equipe
- Longo prazo (> 3 anos)
- Requisito de on-premises

### Vantagens do EJBCA-CE

#### 1. Controle total

- Você decide tudo: algoritmos, validades, extensões X.509
- Pode criar hierarquias complexas (múltiplas CAs, sub-CAs)
- Perfis de certificado ilimitados e totalmente customizáveis

#### 2. Independência de vendor

- Não há lock-in com nenhum provedor cloud
- Pode migrar entre clouds ou on-premises
- Não depende de decisões de roadmap de terceiros

#### 3. Conformidade regulatória

- Ideal para requisitos de dados sensíveis que não podem sair do país
- Suporte completo a padrões ICP-Brasil e eIDAS
- Auditoria completa de todos os eventos

#### 4. Protocolos completos

- Suporte nativo a ACME, SCEP, EST, CMP
- Integração com qualquer tipo de dispositivo ou aplicação
- APIs REST completas

#### 5. Ambiente de aprendizado

- Entender PKI profundamente
- Treinar equipe em operações de CA
- Experimentar sem custos

### Vantagens do AWS Private CA

#### 1. Simplicidade operacional

- Setup em minutos (não horas/dias)
- Não requer expertise profunda em PKI
- Atualizações automáticas

#### 2. Integração com AWS

- Nativo com ACM (AWS Certificate Manager)
- Integração com CloudTrail, CloudWatch, IAM
- Suporta diretamente serviços AWS (ELB, API Gateway)

#### 3. Alta disponibilidade nativa

- Multi-AZ por padrão
- 99.9% SLA
- Backup automático

#### 4. Segurança gerenciada

- Chaves protegidas em CloudHSM gerenciado
- Criptografia at-rest e in-transit por padrão
- Conformidade com SOC, PCI DSS, HIPAA

#### 5. Escalabilidade automática

- Não há limite de certificados
- Performance consistente independente do volume
- Não requer planejamento de capacidade

## Casos de uso ideais para cada solução

### Use EJBCA-CE quando:

✅ **Requisitos regulatórios de soberania de dados**
- Dados não podem sair do país ou da rede interna
- Conformidade ICP-Brasil requer CA on-premises

✅ **Ambiente de desenvolvimento e testes**
- Emitir certificados de teste sem custos
- Experimentar com configurações de PKI

✅ **Volume muito alto de certificados**
- > 50.000 certificados/ano
- Custo por certificado se torna crítico

✅ **Protocolos específicos necessários**
- SCEP para dispositivos de rede
- EST para IoT
- CMP para workflows empresariais

✅ **On-premises ou air-gapped**
- Data centers próprios
- Redes isoladas sem acesso à internet
- Ambientes industriais (OT)

✅ **Customização extrema**
- Perfis de certificado muito específicos
- Extensões X.509 personalizadas
- Workflows de aprovação complexos

### Use AWS Private CA quando:

✅ **Infraestrutura já está na AWS**
- Aproveitar integração nativa
- Simplificar operações

✅ **Equipe pequena ou sem expertise em PKI**
- Não há recursos para operar CA
- Foco deve estar em outras prioridades

✅ **Necessidade de time-to-market rápido**
- Projeto precisa de PKI em dias (não meses)
- Proof of concept precisa estar online rapidamente

✅ **Volume baixo a médio**
- < 20.000 certificados/ano
- Custo operacional supera custo de serviço gerenciado

✅ **Conformidades cloud-native**
- SOC 2, PCI DSS, HIPAA já cobertas pela AWS
- Auditorias simplificadas com CloudTrail

✅ **Integração com ACM**
- Certificados para ELB, CloudFront, API Gateway
- Renovação automática via ACM

## Arquitetura híbrida

Uma estratégia interessante é combinar ambas as soluções:

### Modelo de referência para Cartório Digital

```
┌────────────────────────────────────────────────────────┐
│              Cartório Digital PKI                      │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │   EJBCA-CE (On-Premises ou VPC privada)          │ │
│  │   ┌────────────────────────────────────────┐     │ │
│  │   │  Root CA (Offline)                     │     │ │
│  │   │  - Validade: 20 anos                   │     │ │
│  │   │  - Armazenamento: HSM físico offline   │     │ │
│  │   └────────────────────────────────────────┘     │ │
│  │                     │                             │ │
│  │   ┌─────────────────┴─────────────────┐          │ │
│  │   │                                   │          │ │
│  │   ▼                                   ▼          │ │
│  │  ┌──────────────────┐   ┌──────────────────┐   │ │
│  │  │ Document Signing │   │ Internal Services│   │ │
│  │  │ Intermediate CA  │   │ Intermediate CA  │   │ │
│  │  └──────────────────┘   └──────────────────┘   │ │
│  │   Emite certificados     Emite certificados     │ │
│  │   para assinatura de     para mTLS interno      │ │
│  │   documentos oficiais                           │ │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │   AWS Private CA (Cloud Pública)                 │ │
│  │   ┌────────────────────────────────────────┐     │ │
│  │   │  Public-Facing TLS CA                  │     │ │
│  │   │  - Integrada com ACM                   │     │ │
│  │   │  - Certificados para ELB, CloudFront   │     │ │
│  │   │  - Renovação automática                │     │ │
│  │   └────────────────────────────────────────┘     │ │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Benefícios desta arquitetura:**

- ✅ Documentos oficiais assinados por CA sob controle total (EJBCA-CE)
- ✅ Serviços internos com PKI customizada (EJBCA-CE)
- ✅ Serviços públicos na internet com operação simplificada (AWS Private CA)
- ✅ Redução de custos (AWS CA apenas para subset de certificados)
- ✅ Redução de complexidade (AWS gerencia parte da infraestrutura)

## Comparação com outras soluções

### Google Cloud Certificate Authority Service (CAS)

| Aspecto | EJBCA-CE | Google CAS |
|---------|----------|------------|
| **Pricing** | Gratuito | $200/mês por CA + $0.30/cert |
| **Protocolos** | ACME, SCEP, EST, CMP | API Google + ACME (beta) |
| **On-premises** | ✅ | ❌ |
| **Customização** | Alta | Média |

**Conclusão:** Similar ao AWS Private CA, mas com preço mais agressivo. Ideal se já está no GCP.

### Azure Key Vault (Managed CA)

| Aspecto | EJBCA-CE | Azure Key Vault |
|---------|----------|-----------------|
| **Pricing** | Gratuito | $3/cert (via DigiCert/GlobalSign) |
| **Protocolos** | ACME, SCEP, EST, CMP | API Azure + ACME |
| **On-premises** | ✅ | ❌ |
| **Customização** | Alta | Baixa (templates fixos) |

**Conclusão:** Mais caro que AWS/GCP. Use apenas se já tem investimento pesado em Azure.

### HashiCorp Vault PKI

| Aspecto | EJBCA-CE | HashiCorp Vault |
|---------|----------|-----------------|
| **Pricing** | Gratuito | Gratuito (OSS) / $0.03/hora (Enterprise) |
| **Foco** | PKI dedicada | Secrets management + PKI |
| **Protocolos** | ACME, SCEP, EST, CMP | API REST + ACME (plugin) |
| **UI Admin** | Completa | Básica |
| **Conformidade** | ICP-Brasil, eIDAS | General purpose |

**Conclusão:** Vault é excelente para PKI efêmera (TTLs curtos, automação máxima). EJBCA-CE é melhor para PKI tradicional com certificados de longa duração.

## Matriz de decisão

| Critério | EJBCA-CE | AWS Private CA | Vault PKI |
|----------|----------|----------------|-----------|
| **Custo (10k certs/ano)** | $$ | $$$ | $ |
| **Operação** | Alta complexidade | Baixa | Média |
| **Time-to-market** | Lento (semanas) | Rápido (horas) | Médio (dias) |
| **Vendor lock-in** | ✅ Nenhum | ❌ Alto | ✅ Baixo |
| **On-premises** | ✅ | ❌ | ✅ |
| **Customização** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Conformidade regulatória** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Curva de aprendizado** | Íngreme | Suave | Média |

## Recomendação para o Cartório Digital

### Estratégia sugerida (3 fases)

#### Fase 1: MVP (3-6 meses)

- **Use:** AWS Private CA
- **Por quê:** Time-to-market rápido, foco em features do cartório
- **Escopo:** Certificados TLS para APIs públicas

#### Fase 2: Expansão (6-12 meses)

- **Use:** EJBCA-CE + AWS Private CA (híbrido)
- **Por quê:** Adicionar PKI interna sem aumentar custos AWS
- **Escopo:** 
  - EJBCA-CE: Certificados de assinatura de documentos, mTLS interno
  - AWS Private CA: Continua gerenciando TLS público

#### Fase 3: Produção madura (12+ meses)

- **Migrar para:** EJBCA-CE completo (on-premises ou VPC dedicada)
- **Por quê:** Redução de custos, conformidade total ICP-Brasil
- **Escopo:** 
  - Root CA offline em HSM físico
  - Múltiplas Intermediate CAs para diferentes propósitos
  - Integração completa com sistemas do cartório

## Conclusão

Não existe solução "melhor" em absoluto. A escolha depende de:

- **Maturidade da organização** em operações de PKI
- **Volume de certificados** esperado
- **Requisitos regulatórios** (ICP-Brasil, dados on-premises)
- **Budget** disponível (CAPEX vs OPEX)
- **Time-to-market** necessário
- **Equipe** disponível para operar

Para o **Cartório Digital**, uma abordagem híbrida ou migração progressiva (AWS → EJBCA-CE) faz mais sentido que um extremo único.

## Próximos passos

No próximo capítulo, você verá **casos de uso práticos do EJBCA-CE no Cartório Digital**, com exemplos concretos de implementação e integração com os módulos do curso.

## Referências

- **AWS Private CA Pricing:** [https://aws.amazon.com/private-ca/pricing/](https://aws.amazon.com/private-ca/pricing/)
- **Google CAS Pricing:** [https://cloud.google.com/certificate-authority-service/pricing](https://cloud.google.com/certificate-authority-service/pricing)
- **EJBCA vs Commercial PKI:** [https://www.ejbca.org/ejbca-vs-commercial-pki/](https://www.ejbca.org/ejbca-vs-commercial-pki/)
- **HashiCorp Vault PKI:** [https://www.vaultproject.io/docs/secrets/pki](https://www.vaultproject.io/docs/secrets/pki)

