# Revogação e validação de certificados

## Exemplo Inspirador

Certa manhã, a equipe do cartório recebeu a notícia de que um colaborador havia perdido o token criptográfico durante uma viagem. Em vez de pânico, veio a ação coordenada: o analista abriu o índice da CA, localizou o serial comprometido e, em poucos minutos, a credencial estava revogada, uma nova CRL publicada e o responder OCSP emitindo alertas em tempo real. O incidente transformou-se em demonstração pública de maturidade, provando que confiança também significa saber retirar acessos com rapidez e clareza.

## Conceitos Fundamentais

- **Revogação:** processo de invalidar certificados antes do vencimento quando a chave é comprometida ou o titular perde autorização.
- **CRL (Certificate Revocation List):** arquivo assinado pela CA com a lista de seriais revogados.
- **OCSP (Online Certificate Status Protocol):** serviço que responde on-line sobre o status de um certificado específico.
- **Validação da cadeia:** combinação de certificados com CRLs/OCSP para garantir que apenas credenciais válidas sejam aceitas.

## Práticas Reais

1. **Revogue o certificado comprometido e gere uma nova CRL:**
   ```bash
   cd ~/pki/intermediate
   openssl ca -config openssl.cnf -status <serial>
   openssl ca -config openssl.cnf -revoke certs/usuario.exemplo.cert.pem
   openssl ca -config openssl.cnf -gencrl -out crl/intermediate.crl.pem
   chmod 444 crl/intermediate.crl.pem
   openssl crl -in crl/intermediate.crl.pem -noout -text | grep usuario.exemplo
   ```
   Registre no diário de incidentes quem autorizou a revogação e como a CRL foi distribuída.

2. **Ofereça validação em tempo real com OCSP:**
   ```bash
   openssl ocsp -port 2560 -text \
       -index index.txt \
       -CA certs/ca-chain.cert.pem \
       -rkey private/intermediate.key.pem \
       -rsigner certs/intermediate.cert.pem
   ```
   Documente o procedimento de subida do serviço e as portas liberadas no firewall.

3. **Ensine as equipes a consultar o status antes de confiar:**
   ```bash
   openssl ocsp -issuer certs/intermediate.cert.pem \
       -cert certs/cartorio.local.cert.pem \
       -url http://localhost:2560
   ```
   Inclua esse comando em roteiros de atendimento para validar credenciais apresentadas pelos cidadãos.

4. **Verifique a cadeia completa com CRL:**
   ```bash
   openssl verify -CAfile certs/ca-chain.cert.pem \
       -crl_download -CRLfile crl/intermediate.crl.pem \
       certs/cartorio.local.cert.pem
   ```
   Utilize o relatório para restabelecer integrações com parceiros após incidentes.

5. **Planeje monitoramento contínuo:** configure tarefas para gerar e publicar CRLs periodicamente e alarmes caso o OCSP pare de responder.

## Gancho para o Próximo Capítulo

Com a confiança protegida de ponta a ponta, estamos prontos para colocar esses certificados em trânsito. No próximo módulo mergulharemos no TLS e mTLS, começando por um exemplo inspirador de como uma conexão segura muda a experiência no balcão digital do cartório.
