# Práticas de Auditoria e Rotação de Chaves

## Boas práticas
- Rotacionar chaves a cada 12 meses.
- Registrar tudo no **AWS CloudTrail**.
- Alertas com **CloudWatch**.
- Verificação de integridade com hashes/assinaturas cruzadas.

### Exemplo prático
```python
kms.enable_key_rotation(KeyId=key_id)
print("Rotação de chave ativada:", key_id)
```
