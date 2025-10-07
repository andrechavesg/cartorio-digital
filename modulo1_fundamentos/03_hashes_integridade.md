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

Os analistas do cartório precisam garantir que o lote de certidões exportado pelo sistema de atendimento não seja alterado antes de chegar ao cofre digital estadual. Eles registram o resumo criptográfico de cada arquivo e armazenam essa referência na ata de transmissão. Assim, qualquer divergência futura pode ser detectada com rapidez.

A ferramenta escolhida é o `openssl dgst`, porque gera hashes com algoritmos reconhecidos pelos órgãos de fiscalização e pode ser automatizada em scripts de auditoria. Depois de entender a motivação, calcule o SHA-256 do arquivo `documento.txt`:

```bash
openssl dgst -sha256 documento.txt
```

O comando exibirá o digest em hexadecimal. Agora edite `documento.txt` (adicione ou remova uma linha) e calcule novamente. O digest será completamente diferente.

Hashes também são usados como parte das assinaturas digitais: em vez de assinar o documento inteiro, assinamos seu hash.

### Exemplo de integridade

Baixe um arquivo qualquer (por exemplo, um programa ou biblioteca) e calcule seu hash. Muitos sites oficiais fornecem o hash esperado do arquivo; você pode comparar com seu resultado para garantir que o download não foi corrompido ou alterado.
