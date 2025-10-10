# Requisitos para Autoridade Certificadora Conforme ITI/ICP-Brasil

Este documento consolida os requisitos técnicos e de negócio de uma Autoridade Certificadora (AC) que pretende ser credenciada junto ao Instituto Nacional de Tecnologia da Informação (ITI), em conformidade com a Infraestrutura de Chaves Públicas Brasileira (ICP-Brasil). Ele resume os principais controles exigidos pelos documentos normativos DOC-ICP e mapeia esses controles para funcionalidades e componentes da plataforma descrita no apêndice `aws-kms-production`.

## 1. Normativos Considerados

| Documento | Escopo | Áreas abordadas |
|-----------|--------|-----------------|
| **DOC-ICP-01** | Requisitos de credenciamento | Governança, dependência hierárquica, políticas e procedimentos |
| **DOC-ICP-02** | Declaração de práticas de certificação (CPS) | Estrutura da CPS, compromissos da AC, responsabilidades |
| **DOC-ICP-03** | Requisitos mínimos de segurança | Segurança física e lógica, controle de acesso, gestão de riscos |
| **DOC-ICP-04** | Auditoria, conformidade e fiscalização | Processos de auditoria, evidências, independência |
| **DOC-ICP-05** | Política de certificados | Conteúdo e regras para emissão, uso e revogação |
| **DOC-ICP-08** | Perfis técnicos de certificados | Campos obrigatórios, extensões, algoritmos criptográficos |
| **DOC-ICP-10** | Requisitos técnicos de hardware e software | HSMs homologados, requisitos de sistemas operacionais e controles |
| **DOC-ICP-15** | Políticas para AC de Carimbo do Tempo (TSA) | Requisitos adicionais para emissão de carimbo do tempo |

> **Referências complementares**: Resoluções do Comitê Gestor da ICP-Brasil, Manual de Credenciamento, RFC 3647 (estrutura de CPS), RFC 5280 (perfil X.509).

## 2. Personas e Responsabilidades

| Persona | Principais responsabilidades | Controles relevantes |
|---------|-----------------------------|----------------------|
| **Administrador da AC** | Configuração do sistema, publicação de políticas, gestão de CA raiz/intermediárias | Segregação de funções (DOC-ICP-03), registro de ações (DOC-ICP-04) |
| **Operador / Oficial de Registro (RA)** | Identificação presencial ou remota, aprovação de solicitações, emissão de certificados | Procedimentos de validação (DOC-ICP-05), trilhas de auditoria |
| **Administrador de Segurança** | Gestão de HSM/KMS, key ceremony, gestão de segredos | Controle em dupla custódia (DOC-ICP-03 e DOC-ICP-10) |
| **Auditor** | Acompanhamento das emissões, análise de logs, emissão de relatórios | Conformidade (DOC-ICP-04), acesso somente leitura |
| **Subscritor / Cliente** | Solicitação e instalação de certificados, renovação, revogação | Requisitos de CPS (DOC-ICP-02), notificações |

### Fluxos Críticos

- **Onboarding do Subscritor**: captura de dados, comprovação documental, validação presencial/remota, aprovação de RA, emissão.
- **Renovação Programada**: notificação automática, coleta de novos dados (quando exigido), validação e emissão sem quebra de cadeia.
- **Revogação Emergencial**: recebimento da solicitação, verificação de identidade, aprovação dual, publicação imediata da CRL e atualização de OCSP.
- **Auditoria Periódica**: extração de logs assinados, revisão de trilhas, geração de relatórios e planos de ação.
- **Key Ceremony**: preparação de sala segura, dupla custódia, geração/rotação de chaves de CA, documentação e assinatura das atas.

## 3. Escopo Funcional Obrigatório

| Domínio | Requisitos | Normativos ligados |
|---------|------------|--------------------|
| **Ciclo de Vida de Certificados** | Emissão, renovação, revogação, suspensão (se aplicável), publicação de CRL | DOC-ICP-05, DOC-ICP-08 |
| **Validação** | OCSP assinado, resposta de carimbo do tempo (quando aplicável) | DOC-ICP-08, DOC-ICP-15 |
| **Identificação e Autenticação** | Identificação presencial/remota, armazenamento de evidências, mecanismos de autenticação forte | DOC-ICP-02, DOC-ICP-03 |
| **Gestão de Chaves** | Geração, custódia, backup e destruição de chaves de AC em HSM homologado; uso de AWS KMS / CloudHSM com validação FIPS 140-2 | DOC-ICP-03, DOC-ICP-10 |
| **Políticas e Documentação** | CPS, CP, Termos de uso, manuais operacionais, planilhas de preparação para auditoria | DOC-ICP-01, DOC-ICP-02 |
| **Auditoria e Registro** | Trilhas de auditoria, retenção de logs por 20 anos, exportação segura para auditorias | DOC-ICP-04 |
| **Segurança Física e Lógica** | Segmentação de rede, controle de acesso físico, monitoramento de salas seguras, RBAC | DOC-ICP-03 |
| **Continuidade de Negócio** | Planos de contingência e DR testados, replicação multi-AZ, exercícios periódicos | DOC-ICP-03 |

### Métricas e SLOs de Referência

| Área | Indicador | Meta sugerida |
|------|-----------|--------------|
| Disponibilidade da AC | SLA mensal | ≥ 99.9% |
| Tempo de emissão | P95 do tempo entre aprovação e emissão | ≤ 5 minutos |
| Revogação | P95 do tempo entre solicitação aprovada e publicação na CRL/OCSP | ≤ 15 minutos |
| Retenção de evidências | Tempo de guarda | ≥ 20 anos |
| Continuidade | RPO / RTO | RPO ≤ 15 minutos; RTO ≤ 2 horas |
| Segurança | Tempo de resposta a incidentes críticos | ≤ 1 hora |

## 4. Controles de Segurança Requeridos

### 4.1 Controles Técnicos

- **Proteção de chaves da AC**: HSM homologado pelo ITI (CloudHSM ou integração com HSM on-premises via AWS Direct Connect) com autenticação dual control e logs imutáveis.
- **Segurança de APIs**: Uso de mTLS, OAuth 2.0 / OpenID Connect com MFA para usuários humanos, tokens curtos para integrações automatizadas.
- **Segurança de Dados**: Criptografia em repouso (KMS) e em trânsito (TLS 1.2+), gestão de segredos via AWS Secrets Manager.
- **Segregação de Ambientes**: Dev, Stage e Prod isolados, com políticas de acesso mínimas e aprovação formal para mudanças (CAB).
- **Proteção de Rede**: VPC isolada, sub-redes privadas, bastion hosts com MFA, WAF/Shield para proteções de camada 7.
- **Monitoramento**: Telemetria via CloudWatch, logs centralizados em OpenSearch/ELK, dashboards de compliance, alarmes em SNS/Slack.

### 4.2 Controles Administrativos

- **Cadeia hierárquica definida**: Root CA offline, Issuing CAs online em ambientes redundantes, TSA separada.
- **Procedimentos Operacionais**: Manual de operação, runbooks de incidentes, plano de key ceremony documentado, periodicidade de testes de DR.
- **Gestão de Pessoas**: Verificação de antecedentes, treinamento em segurança, termos de confidencialidade, rotação periódica de tarefas críticas.
- **Gestão de Terceiros**: Acordos de confidencialidade, contrato de SLA com provedores (AWS, auditorias externas), due diligence.
- **Gestão de Mudanças**: Uso de ferramenta ITSM, approvals multi-nível, versionamento automatizado da infraestrutura (GitOps).

## 5. Mapeamento para Componentes da Plataforma

| Controle | Capítulo/Componente relacionado |
|----------|--------------------------------|
| Key ceremony & HSM | `02_aws_kms.md`, módulos Terraform de KMS/CloudHSM, scripts de cerimônia |
| Emissão/Revogação de certificados | Backend CA Service, Publisher Service, `05_iac_terraform.md` (pipeline) |
| OCSP e TSA | Serviços dedicados (Validation Service e Timestamping Service), CloudWatch dashboards |
| Auditoria e logging | Stack OpenSearch/CloudWatch, procedimentos no capítulo de observabilidade |
| Políticas e CPS | Repositório de documentação (`08_disaster_recovery.md` e futuros anexos) |
| Continuidade | Planos de DR, replicação cross-region, scripts de backup automatizado |

## 6. Requisitos de Dados e Retenção

- **Metadados de Certificados**: armazenamento em banco relacional (RDS/Aurora) com replicação multi-AZ e criptografia KMS.
- **CRL e OCSP**: publicação em S3 versionado e distribuição via CloudFront/WAF; respostas OCSP cacheadas com validade controlada.
- **Trilhas de Auditoria**: logs estruturados enviados para OpenSearch e arquivados em S3 Glacier com retenção mínima de 20 anos.
- **Evidências de Identificação**: armazenamento seguro em S3 com bucket dedicado, criptografia por objeto, política de acesso com prazo definido.
- **Documentos de Key Ceremony**: assinatura digital, armazenamento redundante e controle de versões.

## 7. Próximos Passos

1. Detalhar a arquitetura de microserviços e fluxos de dados considerando os controles acima.
2. Especificar requisitos funcionais e não funcionais para cada serviço (SLO, disponibilidade, RTO/RPO).
3. Preparar a documentação da CPS/CP alinhada com DOC-ICP-02.
4. Definir métricas de conformidade para monitoramento contínuo e automações de evidência.
5. Atualizar o plano de implementação e priorizar entregas conforme riscos identificados.

---

**Responsável pela atualização**: Equipe de Arquitetura & Compliance

**Última atualização**: <!-- DATE_PLACEHOLDER -->

