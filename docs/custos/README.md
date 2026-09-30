# Estimativa de custos

Estimativas feitas na **Google Cloud Pricing Calculator** (<https://cloud.google.com/products/calculator>), com preços vigentes em 29/09/2026, região **southamerica-east1**. A análise completa está em [`../arquitetura.md`](../arquitetura.md), seção 5.9.

## Arquivos desta pasta

| Arquivo | Conteúdo |
|---|---|
| `estimativa-cenario-a.csv` | Exportação da calculadora: cenário A, operação contínua (730 h/mês) |
| `estimativa-cenario-b.csv` | Exportação da calculadora: cenário B, horas de trabalho da Entrega 2 |

## Links das estimativas

- **Cenário A (730 h/mês): US$ 75,25/mês** — <https://cloud.google.com/calculator?dl=CjhDaVJqTVRjMFpXWmxOQzFpTkRRM0xUUTFOekF0T0dFMk55MDJORFE0TWprMU9ERTNZak1RQVE9PRokMjk0QzgxQUItRTBGQS00NkExLUExRUYtRjYxNkMxNEIwMEVD>
- **Cenário B (~100 h por VM): US$ 21,39 na calculadora, US$ 13,33 com o ajuste de IP e NAT** — <https://cloud.google.com/products/calculator?dl=CjhDaVEwTldObE9HRTNPUzFtTlRRMExUUTBOV1V0T1dabE1DMDBOakZoWTJNM1pXWTJNRGtRQVE9PRAIGiRDQjk3NDhBMS0wOUQyLTQ4NUQtOERDRS1DQTA2MDk0NDZDMUU>

## O que foi lançado na calculadora

| Item na calculadora | Configuração | Cenário A (mensal) | Cenário B (calculadora) |
|---|---|---|---|
| Compute Engine — VMs e2-micro (bastion + frontend) | 2 instâncias, SO gratuito (Ubuntu), Regular, 10 GB *Standard persistent disk* cada | US$ 20,61 | US$ 3,26 |
| Compute Engine — VMs e2-small (backend + db) | 2 instâncias, SO gratuito (Ubuntu), Regular, 20 GB *Balanced persistent disk* cada | US$ 44,83 | US$ 8,32 |
| Networking → IP Address | 2 IPs em uso em VMs padrão; 0 IPs reservados sem uso | US$ 3,65 | US$ 3,65 |
| Networking → NAT Gateway | Public NAT, 2 VMs atribuídas, 2 GiB processados, 1 IP | US$ 5,78 | US$ 5,78 |
| Networking → Data Transfer | Premium tier, 2 GiB de São Paulo para a América do Sul; tráfego interno 0 (mesma zona, sem cobrança) | US$ 0,38 | US$ 0,38 |
| **Total** | | **US$ 75,25** | **US$ 21,39** |

**Horas no Compute Engine:** a calculadora pede o total de horas de **todas** as instâncias do item. No cenário A são 2 × 730 = **1460 h**; no cenário B, **100 h por VM**.

**IP Address e NAT Gateway não têm campo de horas** na calculadora: ela sempre considera o mês inteiro (730 h). No cenário B, o custo desses dois itens foi calculado proporcionalmente (valor mensal × 100 / 730), porque eles são destruídos junto com as VMs entre as sessões. Com esse ajuste, o custo esperado do cenário B é **US$ 13,33**. Detalhes na seção 5.9 do `arquitetura.md`.

**Link no final dos CSVs:** o arquivo do cenário B foi gerado a partir de uma cópia da estimativa do cenário A, e por isso a última linha do CSV repete o link do cenário A. O link correto de cada cenário é o listado acima.
