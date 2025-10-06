# Revogação e validação de certificados

À medida que emitimos certificados, também precisamos considerar o que acontece quando uma chave é comprometida ou o titular não deve mais ter acesso. A revogação permite invalidar certificados antes do vencimento e notificar clientes de que eles não devem mais ser confiáveis.

## 1. Revogando um certificado

No OpenSSL, você mantém um arquivo `index.txt` e uma lista de certificados revogados (`crl`). Para revogar um certificado, identifique seu número de série e execute:

```bash
cd ~/pki/intermediate
# Liste certificados emitidos e encontre o serial do certificado do cliente
openssl ca -config openssl.cnf -status <serial>

# Revogue o certificado
openssl ca -config openssl.cnf -revoke certs/usuario.exemplo.cert.pem
```

Em seguida, gere uma nova lista CRL:

```bash
openssl ca -config openssl.cnf -gencrl -out crl/intermediate.crl.pem
chmod 444 crl/intermediate.crl.pem
```

Verifique se o certificado aparece como revogado:

```bash
openssl crl -in crl/intermediate.crl.pem -noout -text | grep usuario.exemplo
```

## 2. Verificando revogação

Os clientes precisam consultar a CRL ou usar OCSP (Online Certificate Status Protocol) para saber se um certificado está válido. Há duas abordagens comuns:

- **CRL offline**: o servidor web carrega a CRL periodicamente e rejeita conexões de certificados revogados.
- **OCSP**: o servidor delega a verificação a um responder OCSP que informa se o certificado está “good”, “revoked” ou “unknown”.

Para servir OCSP com OpenSSL:

```bash
# Rode um responder OCSP usando a CA intermediária
openssl ocsp -port 2560 -text \
    -index index.txt \
    -CA certs/ca-chain.cert.pem \
    -rkey private/intermediate.key.pem \
    -rsigner certs/intermediate.cert.pem
```

No cliente, consulte o status do certificado:

```bash
openssl ocsp -issuer certs/intermediate.cert.pem \
    -cert certs/cartorio.local.cert.pem \
    -url http://localhost:2560
```

Você também pode adicionar a URL do OCSP ao campo “Authority Information Access” ao emitir certificados, para que navegadores consultem automaticamente.

## 3. Validando cadeias de confiança

Para verificar se um certificado é válido e não está revogado:

```bash
# Verifique a cadeia e revogação usando a CRL
openssl verify -CAfile certs/ca-chain.cert.pem \
    -crl_download -CRLfile crl/intermediate.crl.pem \
    certs/cartorio.local.cert.pem
```

A opção `-crl_download` permite que o OpenSSL baixe a CRL indicada no certificado, se estiver configurada.

Com revogação e verificação automatizadas, garantimos que o cartório digital não aceitará credenciais comprometidas. No próximo módulo, usaremos esses certificados para proteger conexões TLS e implementar mTLS.
