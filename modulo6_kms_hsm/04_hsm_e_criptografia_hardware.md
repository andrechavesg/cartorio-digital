# HSM e Criptografia de Hardware

A camada de HSM (Hardware Security Module) garante que chaves críticas permaneçam dentro de um dispositivo com controles físicos e lógicos robustos.

## Benefícios principais

1. **Armazenamento seguro:** chaves privadas nunca deixam o hardware; exportação é bloqueada por design.
2. **Processamento isolado:** operações de assinatura e descriptografia ocorrem dentro do módulo, reduzindo a superfície de ataque.
3. **Conformidade:** necessário para certificações como FIPS 140-2 Nível 3, requisito para Autoridades Certificadoras na ICP-Brasil e QTSPs europeus.
4. **Alta disponibilidade:** clusters HSM oferecem replicação e failover, evitando *single point of failure*.

## Arquitetura recomendada

```
Aplicação → API interna → AWS KMS → (CloudHSM / HSM dedicado)
                        ↘ CloudTrail / SIEM
```

1. A aplicação invoca um serviço interno (por exemplo, `kms-service`) que abstrai as operações de assinatura.
2. O KMS envia a requisição ao cluster HSM para execução dentro do hardware.
3. Logs de auditoria são encaminhados ao SIEM e correlacionados com eventos de aplicação.

## Opções de implantação

- **CloudHSM (AWS):** cluster gerenciado, integra-se ao KMS via *custom key store*. Adequado para produção.
- **Appliance on-premises:** indicado quando a legislação exige data center próprio. Necessita rede dedicada, energia redundante e equipe treinada.
- **HSM portátil:** útil para laboratórios, provas de conceito e recuperação de desastre offline.

## Fluxo de geração de chaves

1. **Inicialização:** duas ou mais pessoas (modelo de múltipla custódia) desbloqueiam o HSM com smartcards ou tokens.
2. **Criação da chave raiz:** executada dentro do HSM; o identificador da chave é exportado para ser referenciado pelo KMS.
3. **Replicação/backup:** cópia criptografada armazenada em cofre físico, seguindo política de desastre.

### Exemplo conceitual (pseudo-código)

```python
def gerar_chave_raiz():
    # Chamada abstrata ao HSM
    handle = hsm_client.generate_key_pair(
        key_type="RSA_4096",
        token_label="CartorioDigitalRAIZ",
        certificate_label="CartorioDigitalRAIZCert"
    )
    return handle
```

> **Dica:** documente o procedimento de rotação e recuperação do HSM; auditores costumam solicitar *runbooks* assinados pelas partes responsáveis.
