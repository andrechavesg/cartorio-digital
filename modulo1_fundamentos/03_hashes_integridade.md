# 3. Funções Hash e Integridade

## Exemplo Inspirador

Uma remessa de certidões recém-assinadas chega ao cofre digital com uma falha misteriosa: um único PDF foi truncado durante o upload e o protocolo quase foi rejeitado. A equipe de infraestrutura reúne-se ao redor do dashboard, calcula rapidamente o hash do arquivo de origem e o compara com o hash armazenado no livro de transmissão. A discrepância acende um alerta vermelho, mas também desperta a certeza de que a integridade pode ser monitorada com maestria. A partir desse episódio, nenhum documento sai do cartório sem o seu resumo criptográfico acompanhado.

## Conceitos Fundamentais

- **Funções hash** transformam qualquer entrada em um resumo de tamanho fixo.
- **Sensibilidade extrema:** alterar um único bit gera um digest completamente diferente (efeito avalanche).
- **Unidirecionalidade:** é inviável recuperar o conteúdo original apenas a partir do hash.
- **Resistência a colisões:** encontrar duas entradas com o mesmo hash é impraticável quando usamos algoritmos modernos como **SHA-256**, **SHA-3** ou **BLAKE3**.
- **Assinaturas digitais dependem de hashes:** em vez de assinar o documento inteiro, assinamos seu digest para obter desempenho e segurança.

## Práticas Reais

1. **Padronize a verificação de remessas:**
   ```bash
   openssl dgst -sha256 documento.txt
   ```
   Registre o valor obtido e inclua-o no livro de transmissão do cartório.

2. **Teste a detecção de alterações:**
   - Edite `documento.txt` adicionando ou removendo uma linha.
   - Recalcule o hash e compare com o original para visualizar a diferença.

3. **Audite downloads oficiais:**
   - Baixe um software ou biblioteca com hash publicado pelo fornecedor.
   - Compare o valor divulgado com o calculado localmente para reforçar a disciplina de integridade.

4. **Prepare-se para assinaturas digitais:**
   - Documente como o hash será integrado ao fluxo jurídico descrito no capítulo anterior.
   - Liste quais algoritmos são aceitos pelo órgão regulador do seu cartório digital.

## Próximos passos

Agora que você domina a arte de detectar qualquer alteração, está pronto para dar o próximo salto: **transformar esses hashes em assinaturas digitais que comprovem autoria e validade jurídica**. No capítulo seguinte veremos, a partir de uma situação real do cartório, como unir chaves e resumos para carimbar documentos com confiança absoluta.
