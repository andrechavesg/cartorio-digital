# HSM e Criptografia de Hardware

O HSM é o **núcleo de confiança** do sistema de certificação.

## Benefícios
- Armazenamento seguro de chaves privadas.
- Processamento criptográfico isolado.
- Conformidade (ICP-Brasil/eIDAS).

## Fluxo no Cartório Digital
1. O KMS interage com o HSM para gerar a chave raiz.
2. Chaves derivadas são usadas para assinar certificados.
3. Material sensível nunca é exposto ao app.

### Exemplo conceitual
```python
# Chamada abstrata ao HSM
def gerar_chave_no_hsm():
    return hsm_client.generate_key_pair(key_type="RSA_2048")
```
