# Transparência dos Certificados como Vitrine Pública

O cartório digital enfrentou críticas quando um cidadão questionou a autenticidade de uma certidão eletrônica emitida meses atrás. Percebemos que, sem visibilidade clara de cada emissão, a confiança seria sempre vulnerável. Decidimos transformar essa tensão em oportunidade: tornar os certificados rastreáveis do começo ao fim.

## Conceito: Linhagem de Certificados
Transparência significa oferecer **linhagem completa** para cada certificado emitido pela Autoridade Certificadora criada no módulo 2. Antes de qualquer consulta técnica, revisitamos o conceito de que toda AC confiável precisa disponibilizar registros públicos de emissão e revogação.

## Passo a Passo Conectado ao Projeto
1. **Revisar as emissões anteriores:** os certificados emitidos no diretório `modulo2_pkicertificados/output/` precisam ser publicados em CT logs e catalogados.
2. **Registrar os hashes:** catalogar os hashes SHA-256 das certidões garante rastreabilidade para cada documento digital.
3. **Sincronizar com o repositório Git:** os fingerprints calculados abastecem o inventário versionado em `modulo2_pkicertificados/inventory/`, permitindo auditoria cruzada com a pipeline de CI/CD do módulo 8.

Somente depois de compreendermos esses dois objetivos partimos para os comandos.

```bash
for cert in modulo2_pkicertificados/output/*.crt; do
  echo "Registrando $(basename "$cert")"
  openssl x509 -in "$cert" -noout -fingerprint -sha256
  curl -X POST https://ct.googleapis.com/logs/argon2024/ct/v1/add-chain \
    -H 'Content-Type: application/json' \
    -d @<(python scripts/build_ct_payload.py "$cert")
done
```

- **Por que funciona:** primeiro calculamos o fingerprint para documentar internamente, depois submetemos ao log Argon usando o payload gerado no script do projeto. O hash é persistido em `inventory/ct-published.csv`, facilitando auditorias posteriores.
- **Resultado inspirado:** cada submissão é uma declaração pública de integridade que reforça a reputação do cartório digital e alimenta indicadores de conformidade exibidos no Grafana.

## Fechando o Loop
Ao tornar cada certificado verificável, fortalecemos a confiança que sustenta os serviços eletrônicos criados nos módulos anteriores. Transparência não é custo; é a vitrine que mostra ao cidadão que o cartório digital está sempre aberto.
