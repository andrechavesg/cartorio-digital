# Práticas de Auditoria e Rotação de Chaves

## Boas práticas
- Rotacionar chaves a cada 12 meses.
- Registrar tudo no **AWS CloudTrail**.
- Alertas com **CloudWatch**.
- Verificação de integridade com hashes/assinaturas cruzadas.

## Políticas de rotação por tipo de chave

### Chaves simétricas (ENCRYPT_DECRYPT)
- Habilite a rotação automática fornecida pelo KMS.
- Documente no runbook a revisão anual do agendamento.
```python
key_metadata = kms.describe_key(KeyId=key_id)["KeyMetadata"]
if key_metadata["KeyUsage"] == "ENCRYPT_DECRYPT":
    kms.enable_key_rotation(KeyId=key_id)
    print("Rotação automática habilitada:", key_id)
```

### Chaves assimétricas (SIGN_VERIFY)
- Não existe rotação automática; mantenha um processo manual documentado.
- Planeje a criação antecipada de uma nova CMK e a troca controlada do alias/ARN nas aplicações.
- Valide as novas assinaturas antes de desativar a chave antiga e arquive as evidências de teste para auditoria.
- Após a migração, desabilite a chave anterior e agende sua exclusão conforme as políticas de retenção.
