# Práticas de Auditoria e Rotação de Chaves

A proteção de chaves vai além da criação inicial. Este capítulo descreve o ciclo de vida completo, controles de auditoria e respostas a incidentes.

## Políticas de rotação

- **Automática (CMKs simétricas):** habilite `enable_key_rotation` para chaves `KeyUsage = "ENCRYPT_DECRYPT"` com `KeySpec = "SYMMETRIC_DEFAULT"`; o KMS executa a rotação anual de forma transparente.
- **Manual (CMKs assimétricas e HSM):** para chaves de assinatura (`KeyUsage = "SIGN_VERIFY"`) com `KeySpec` assimétrica (RSA/ECC) ou materiais hospedados em HSMs externos, defina calendário com *change windows* aprovadas pelo comitê de segurança e execute o plano de migração de alias/ARN documentado.
- **Documentação:** mantenha histórico de versões, motivos da rotação, responsáveis e artefatos de teste em cada ciclo.

### Script de exemplo (Python/boto3)

```python
import boto3


def ativar_rotacao(key_id: str) -> None:
    kms = boto3.client("kms")
    metadata = kms.describe_key(KeyId=key_id)["KeyMetadata"]

    if metadata["KeySpec"] != "SYMMETRIC_DEFAULT":
        print(
            "Chave",
            key_id,
            "usa KeySpec",
            metadata["KeySpec"],
            "e requer rotação manual planejada (sem chamada a enable_key_rotation).",
        )
        return

    kms.enable_key_rotation(KeyId=key_id)
    print("Rotação automática habilitada para:", key_id)
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
