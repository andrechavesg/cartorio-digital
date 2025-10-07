# Visão Inspiradora de Observabilidade para o Cartório Digital

O cartório digital vinha celebrando cada ato eletrônico como conquista coletiva, mas a equipe sentia ansiedade diante da possibilidade de qualquer certificado ser questionado em público sem prova clara de confiança. Foi nesse momento que compreendemos que **observabilidade** seria nossa bússola para transformar insegurança em narrativa de transparência.

## Conceito: Transparência como Pilar
Antes de executar qualquer comando, reforçamos a base conceitual: observabilidade em PKI é a capacidade de transformar emissões de certificados em evidências verificáveis por qualquer cidadão. Ao publicar certificados nos **Certificate Transparency (CT) logs**, garantimos que a história do cartório esteja aberta para auditoria contínua.

## Conectando ao Projeto Principal
No módulo 2 construímos a AC interna e no módulo 3 ativamos TLS para os serviços do cartório. Agora, precisamos garantir que essa cadeia de confiança seja visível publicamente. Observabilidade fecha o ciclo ao permitir que auditores validem cada emissão do repositório `cartorio-digital`.

## Exemplo Guiado: Consultando CT Logs via crt.sh
Já conscientes da importância dos CT logs, utilizamos o `crt.sh` para verificar se o certificado do domínio `cartorio.digital.gov.br` está presente. Antes de qualquer execução, revisamos como os dados retornam em formato JSON e como esse formato será consumido pelos nossos scripts de auditoria criados no diretório `scripts/`. Só depois de compreender o propósito (dar transparência) recorremos ao comando:

```bash
# Por que: buscar evidências públicas de emissão e alimentar os painéis internos.
curl "https://crt.sh/?q=cartorio.digital.gov.br&output=json" | jq '.[0] | {log: .issuer.name, data_emissao: .not_before}'
```

- **O que observamos:** o campo `log` mostra em qual log público o certificado foi registrado e o `not_before` revela quando a emissão ocorreu.
- **Como isso inspira a equipe:** cada linha recebida confirma que os atos do cartório deixam pegadas verificáveis e alimenta relatórios executivos consumidos pela gerência.

Complementamos a investigação com o script `scripts/ct_dashboard.sh`, que transforma o JSON em métricas para o Prometheus introduzido mais adiante neste módulo, consolidando a integração com a infraestrutura do projeto principal.

## Próximos Passos
Compreender CT logs é o primeiro passo. Os capítulos seguintes exploram como monitorar revogação, métricas e alertas, transformando observabilidade em cultura diária.
