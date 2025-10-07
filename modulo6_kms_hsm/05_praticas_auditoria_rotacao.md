# Práticas de Auditoria e Rotação de Chaves

A proteção de chaves vai além da criação inicial. Este capítulo descreve o ciclo de vida completo, controles de auditoria e respostas a incidentes.

## Políticas de rotação

- **Automática (KMS):** habilite `enable_key_rotation` para CMKs de produção (rotação anual).
- **Manual (HSM):** defina calendário com *change windows* aprovadas pelo comitê de segurança.
- **Documentação:** mantenha histórico de versões, motivos da rotação e responsáveis.

### Script de exemplo (Python/boto3)

## Políticas de rotação por tipo de chave

Após um incidente interno em que uma chave operacional permaneceu ativa além do ciclo previsto, elevando o risco de credenciais estagnadas em produção, o time do cartório decidiu formalizar um plano de rotação proativa. A partir dessa experiência, as diretrizes abaixo foram estruturadas para garantir que todas as chaves críticas tenham renovação e rastreabilidade alinhadas às exigências regulatórias do serviço.

### Chaves simétricas (ENCRYPT_DECRYPT)
- Habilite a rotação automática fornecida pelo KMS.
- Documente no runbook a revisão anual do agendamento.

Quando o pipeline de inventário detecta que uma chave simétrica essencial ao processamento dos títulos digitais está prestes a atingir a data de expiração acordada com os auditores, o time de segurança utiliza o script abaixo para responder rapidamente. O código automatiza a habilitação da rotação para a CMK identificada, garantindo que uma nova versão seja provisionada antes de qualquer interrupção operacional, e gera o log que será anexado ao dossiê do projeto como evidência de mitigação de risco.
```python
key_metadata = kms.describe_key(KeyId=key_id)["KeyMetadata"]
if key_metadata["KeyUsage"] == "ENCRYPT_DECRYPT":
    kms.enable_key_rotation(KeyId=key_id)
    print("Rotação automática habilitada:", key_id)
```

O relatório do pipeline, combinado com o output do script, é anexado ao repositório de auditoria e compõe o conjunto de provas utilizado pelos controles de compliance do cartório digital para demonstrar a efetividade da rotação de chaves simétricas.

### Chaves assimétricas (SIGN_VERIFY)
- A aplicação de assinatura do Cartório Digital utiliza CMKs `RSA_2048`, que não suportam a API de rotação automática.
- Execute rotação manual coordenada: abra uma mudança, crie uma nova CMK com a mesma política, republique os aliases
  (`alias/cartorio-assinatura`), ajuste permissões de IAM e valide a emissão dos selos digitais na nova chave.
- Registre em ata os testes de assinatura/verificação, o cutover do alias e a desativação programada da chave anterior.
- Agende a exclusão somente após a confirmação da equipe jurídica e arquive as evidências de auditoria.
```python
key_metadata = kms.describe_key(KeyId=key_id)["KeyMetadata"]
if key_metadata["KeyUsage"] == "SIGN_VERIFY" and key_metadata["KeySpec"].startswith("RSA_"):
    raise RuntimeError(
        "Rotação automática indisponível para CMKs de assinatura RSA; siga o runbook de rotação manual."
    )
```

## Auditoria contínua

1. **CloudTrail + Lake Formation:** armazene logs imutáveis por no mínimo 5 anos.
2. **Integração com SIEM:** crie alertas para eventos `DisableKey`, `ScheduleKeyDeletion` e acessos fora do horário comercial.
3. **Relatórios mensais:** consolide métricas de uso, falhas e tentativas negadas; compartilhe com o time de compliance.

## Controles complementares

- **MFA e separação de funções:** operadores que aprovam o uso da chave não devem ter acesso direto ao código.
- **Break-glass:** defina procedimento emergencial com tokens selados em cofre físico, monitorado por circuito interno.
- **Testes de restauração:** realize simulações trimestrais de recuperação de chave a partir do backup do HSM.

## Resposta a incidentes

1. **Revogação imediata:** use `schedule_key_deletion` (com janelas curtas) ou inative a chave no HSM.
2. **Geração de novos certificados:** reprovisione certificados dependentes da chave comprometida.
3. **Comunicação oficial:** notifique ITI/ICP-Brasil quando aplicável e emita boletim para clientes.
4. **Lições aprendidas:** atualize playbooks e automatizações para evitar recorrência.
