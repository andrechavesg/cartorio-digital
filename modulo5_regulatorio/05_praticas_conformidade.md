# Práticas de conformidade para o cartório digital

Agora que você conhece as principais regulamentações e categorias de assinatura, é hora de aplicar esses conhecimentos ao projeto do cartório digital. Este capítulo apresenta práticas para garantir que sua solução esteja em conformidade com ICP‑Brasil, eIDAS/ETSI e outras normas aplicáveis.

## Checklist de conformidade

1. **Políticas e procedimentos**: Documente políticas internas para gestão de chaves, emissão e revogação de certificados, uso de assinatura qualificada/avançada e carimbo do tempo. Mapeie‑as aos requisitos da DPC e das normas ETSI.
2. **Gestão de chaves e HSM**: Garanta que as chaves privadas das autoridades certificadoras internas e dos oficiais do cartório sejam geradas e armazenadas em dispositivos que atendam ao FIPS 140‑3 ou QSCD (ver próximo módulo). Implemente rotação periódica.
3. **Validação de cadeia**: Configure seu sistema para validar cadeias ICP‑Brasil e, opcionalmente, cadeias eIDAS. Mantenha repositórios de certificados raiz confiáveis.
4. **Revogação e logs de auditoria**: Monitore CRLs e servidores OCSP; implemente log de auditoria para todas as operações de assinatura e emissão. Use logs de Transparência de Certificados (CT) quando disponíveis.
5. **Privacidade e proteção de dados**: Atenda à Lei Geral de Proteção de Dados (LGPD) ao armazenar e processar dados pessoais. Limite acesso à chave privada do usuário e registre consentimentos.

## Propondo ajustes no projeto

- **Integração com AC qualificada**: Se o cartório oferecer atos que requeiram assinatura qualificada, integre‑se a uma AC credenciada ou torne‑se AC. Avalie custos e requisitos de acreditação no ITI.
- **Compatibilidade e interoperabilidade**: Adicione suporte para certificados estrangeiros (eIDAS) no módulo de validação. Defina política de aceitação e processamento de assinaturas avançadas de outras jurisdições.
- **Auditoria externa**: Programe auditorias regulares com avaliadores credenciados para garantir a conformidade contínua. Use checklists baseados na EN 319 401 e na DPC aplicável.
- **Atualizações legislativas**: Mantenha‑se atualizado sobre alterações legais (novas leis, decretos, resoluções do CNJ/ITI) e revise seus procedimentos quando necessário.

### Atividades

1. Elabore um **relatório comparativo** entre os requisitos da ICP‑Brasil e da EN 319 411‑2 para uma AC qualificada. Identifique gaps que seu cartório digital precisará preencher para ser reconhecido como QTSP.
2. Crie um **checklist de conformidade** para uma emissão de certidão eletrônica que utiliza assinatura qualificada. Execute o checklist com seu sistema atual e identifique melhorias a implementar.
3. Pesquise as sanções previstas pela LGPD para vazamento de dados no contexto de certificados digitais e proponha controles para mitigá‑las.
