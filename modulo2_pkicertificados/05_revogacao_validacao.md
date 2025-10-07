# Revogação e validação de certificados

À medida que emitimos certificados, também precisamos considerar o que acontece quando uma chave é comprometida ou o titular não deve mais ter acesso. A revogação permite invalidar certificados antes do vencimento e notificar clientes de que eles não devem mais ser confiáveis.

## 1. Revogando um certificado

Quando um colaborador deixou o cartório e esqueceu de devolver o token, percebemos a dor de manter certificados ativos sem controle. Para resolver o risco, usamos os registros do `index.txt` e a lista de revogação (`crl`) para localizar o serial comprometido e aplicar `openssl ca -revoke`, garantindo que a credencial fosse anulada imediatamente. **Cenário – certificado comprometido:** precisamos remover rapidamente o acesso indevido para preservar a operação.

**Impacto na operação segura:** revogar rápido impede que APIs e guichês digitais aceitem sessões não autorizadas.

```bash
cd ~/pki/intermediate
# Liste certificados emitidos e encontre o serial do certificado do cliente
openssl ca -config openssl.cnf -status <serial>

# Revogue o certificado
openssl ca -config openssl.cnf -revoke certs/usuario.exemplo.cert.pem
```

Em seguida, percebemos que os sistemas externos precisavam receber a notícia da revogação. Geramos uma nova CRL com `openssl ca -gencrl`, distribuindo o arquivo atualizado para que todos rejeitassem a credencial cancelada. **Cenário – certificado comprometido:** parceiros precisam bloquear imediatamente a credencial vazada.

**Impacto na operação segura:** a CRL atualizada sincroniza o bloqueio em todos os pontos de confiança.

```bash
openssl ca -config openssl.cnf -gencrl -out crl/intermediate.crl.pem
chmod 444 crl/intermediate.crl.pem
```

Para auditar o resultado, confirmamos com `openssl crl` que o certificado revogado aparece na lista, encerrando o incidente com transparência. **Cenário – certificado comprometido:** a equipe de resposta precisa de evidências formais da revogação.

**Impacto na operação segura:** a auditoria documentada sustenta relatórios e liberações pós-incidente.

```bash
openssl crl -in crl/intermediate.crl.pem -noout -text | grep usuario.exemplo
```

## 2. Verificando revogação

Após alguns incidentes, percebemos que clientes e integrações ainda confiavam em certificados revogados por falta de consulta em tempo real. A dor era monitorar a validade continuamente; por isso reforçamos duas estratégias: CRL offline ou OCSP, destacando como cada uma endereça o problema.

- **CRL offline**: o servidor web carrega a CRL periodicamente e rejeita conexões de certificados revogados.
- **OCSP**: o servidor delega a verificação a um responder OCSP que informa se o certificado está “good”, “revoked” ou “unknown”.

Quando precisávamos de respostas instantâneas sobre possíveis comprometimentos, montamos um responder OCSP com `openssl ocsp`. Ele consulta o `index.txt` e responde aos clientes em segundos, eliminando a insegurança do time de operações. **Cenário – certificado comprometido:** é necessário informar em tempo real que o certificado não deve mais ser aceito.

**Impacto na operação segura:** um OCSP ativo evita transações com credenciais revogadas em sistemas críticos.

```bash
# Rode um responder OCSP usando a CA intermediária
openssl ocsp -port 2560 -text \
    -index index.txt \
    -CA certs/ca-chain.cert.pem \
    -rkey private/intermediate.key.pem \
    -rsigner certs/intermediate.cert.pem
```

Do lado do cliente, ensinamos as equipes a validar o status usando também `openssl ocsp`, garantindo que qualquer estação do cartório consiga conferir a saúde das credenciais antes de confiar nelas. **Cenário – certificado comprometido:** o atendente precisa confirmar se a credencial apresentada ainda é confiável.

**Impacto na operação segura:** a validação local impede que documentos sejam protocolados com certificados revogados.

```bash
openssl ocsp -issuer certs/intermediate.cert.pem \
    -cert certs/cartorio.local.cert.pem \
    -url http://localhost:2560
```

Você também pode adicionar a URL do OCSP ao campo “Authority Information Access” ao emitir certificados, para que navegadores consultem automaticamente.

## 3. Validando cadeias de confiança

Quando integramos com órgãos parceiros, eles exigiram provas de que nossos certificados estavam corretos e sem pendências de revogação. Resolvido com `openssl verify`, conseguimos demonstrar a integridade da cadeia e a observância da CRL em um único comando, reforçando a confiança na plataforma. **Cenário – certificado comprometido:** antes de restabelecer integrações, precisamos mostrar que o incidente está contido.

**Impacto na operação segura:** a verificação garante que o ecossistema volte a operar apenas com certificados válidos.

```bash
# Verifique a cadeia e revogação usando a CRL
openssl verify -CAfile certs/ca-chain.cert.pem \
    -crl_download -CRLfile crl/intermediate.crl.pem \
    certs/cartorio.local.cert.pem
```

A opção `-crl_download` permite que o OpenSSL baixe a CRL indicada no certificado, se estiver configurada.

Com revogação e verificação automatizadas, garantimos que o cartório digital não aceitará credenciais comprometidas. No próximo módulo, usaremos esses certificados para proteger conexões TLS e implementar mTLS.
