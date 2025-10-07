# 4. Assinaturas Digitais

## Exemplo Inspirador

A sessão de assinatura de uma escritura de compra e venda começa com expectativa. A tabeliã posiciona o token conectado ao HSM, confirma a identidade dos envolvidos e aciona o fluxo automatizado: o documento `contrato.txt` é preparado, o hash é calculado e a assinatura digital é aplicada diante das partes. Quando o comprovante eletrônico aparece com o carimbo “válido”, um silêncio reverente toma conta da sala. O cartório prova que a confiança pode ser traduzida em bytes.

## Conceitos Fundamentais

- **Assinatura digital** combina a força das chaves assimétricas com a precisão das funções hash.
- **Passos essenciais:**
  1. Calcular o hash do documento.
  2. Cifrar o hash com a **chave privada** do signatário (gerando a assinatura).
  3. Distribuir a **chave pública** para que qualquer pessoa valide a assinatura.
- **Garantias oferecidas:** autenticidade, integridade e não repúdio.
- **Conformidade:** a cadeia de certificação (ICP-Brasil, no nosso contexto) precisa estar válida para que a assinatura tenha valor jurídico.

## Práticas Reais

1. **Prepare o ambiente:** gere um par de chaves RSA se ainda não possuir e crie o arquivo `contrato.txt` com o texto da escritura.

2. **Assine com propósito:**
   ```bash
   openssl dgst -sha256 -sign rsa_private.pem -out contrato.sig contrato.txt
   ```
   Registre quem autorizou a assinatura e onde a chave privada está armazenada (idealmente, em HSM ou cofre seguro).

3. **Verifique com rigor:**
   ```bash
   openssl dgst -sha256 -verify rsa_public.pem -signature contrato.sig contrato.txt
   ```
   Documente a evidência de que o documento permaneceu íntegro desde a sessão de assinatura.

4. **Comprove a cadeia de confiança:**
   ```bash
   openssl verify -CAfile cadeia_ca.pem certificado_tabelia.pem
   ```
   Garanta que a hierarquia ICP-Brasil esteja atualizada e arquive o relatório para auditorias futuras.

5. **Experimente a resiliência:** altere `contrato.txt` e repita a verificação para sentir, na prática, como a assinatura denuncia qualquer mudança.

## Gancho para o Próximo Capítulo

Você agora domina os blocos fundamentais: cifras, chaves, hashes e assinaturas. O próximo passo é ousado e inspirador — construir uma **infraestrutura de chave pública completa** para emitir certificados confiáveis. No início do próximo módulo abriremos o capítulo de PKI mostrando como esse poder se organiza em autoridades certificadoras a serviço do cartório digital.
