# 3. Funções Hash e Integridade

As funções hash são essenciais para garantir a integridade dos dados. Elas recebem qualquer entrada e produzem um **resumo (digest)** de tamanho fixo. Pequenas alterações na entrada resultam em resumos totalmente diferentes.

## Propriedades importantes

- **Unidirecionalidade:** dado um hash, é praticamente impossível descobrir a entrada original.
- **Colisão resistente:** é improvável encontrar duas entradas diferentes que gerem o mesmo hash.
- **Determinística:** a mesma entrada sempre produz o mesmo hash.

Algoritmos populares:
- **SHA-2** (como SHA-256, SHA-384 e SHA-512).
- **SHA-3**, a família mais recente.
- **BLAKE2** e **BLAKE3**, rápidos e seguros.

## Exemplo prático

Em um caso recente, uma remessa de certidões assinadas chegou corrompida ao cofre digital: um PDF foi cortado no meio durante o upload e ninguém percebeu até o protocolo ser rejeitado. Para evitar repetição do incidente, os analistas decidiram registrar o resumo criptográfico de cada arquivo no livro de transmissão, comparando-o no destino para identificar qualquer alteração antes de nova tentativa de registro.

Poucos dias depois, uma unidade do interior relatou que os downloads do sistema central eventualmente vinham truncados quando a conexão caía. Para investigar, o time de infraestrutura passou a calcular hashes locais e cruzá-los com os valores publicados no portal, barrando arquivos suspeitos antes de serem anexados ao processo eletrônico.

Com esses dois incidentes documentados, o comitê técnico padronizou o uso do `openssl dgst`, ferramenta que gera hashes reconhecidos pelos órgãos de fiscalização e pode ser automatizada em scripts de auditoria. Só então o operador responsável executa o comando para calcular o SHA-256 do arquivo `documento.txt`:

```bash
openssl dgst -sha256 documento.txt
```

O comando exibirá o digest em hexadecimal. Agora edite `documento.txt` (adicione ou remova uma linha) e calcule novamente. O digest será completamente diferente, permitindo detectar corrupções como as do PDF e validar downloads como no incidente relatado.

Hashes também são usados como parte das assinaturas digitais: em vez de assinar o documento inteiro, assinamos seu hash.

### Exemplo de integridade

Baixe um arquivo qualquer (por exemplo, um programa ou biblioteca) e calcule seu hash. Muitos sites oficiais fornecem o hash esperado do arquivo; você pode comparar com seu resultado para garantir que o download não foi corrompido ou alterado.
