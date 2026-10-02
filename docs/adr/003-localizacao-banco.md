# ADR-003: Localização do banco de dados

- **Status:** Aceito
- **Data:** 2026-09-30
- **Decisores:** Grupo 6 (Lipe, Eduardo)
- **Contexto do projeto:** ZadInventory na Google Cloud (`southamerica-east1`), Entrega 1 do Projeto Integrador

## Y-statement

No contexto de onde hospedar o MySQL do ZadInventory, que hoje roda no Cloud SQL do deploy anterior em Cloud Run,
diante da necessidade de isolar o banco da aplicação, mantê-lo fora do alcance da internet sob as mesmas regras de firewall por instância do restante do projeto e controlar o custo pago pelo grupo,
decidimos rodar o **MySQL 8.4 em uma VM privada dedicada** (`vm-db`, 10.10.2.20, sem IP externo),
e descartamos o Cloud SQL gerenciado e colocar o MySQL na mesma VM do backend,
para ter isolamento e controle total do banco e poder destruí-lo e recriá-lo junto com o resto do ambiente entre as sessões de teste,
aceitando que o grupo passa a ser responsável por toda a operação do banco (backup, restauração, atualizações de segurança e tuning), que a `vm-db` é um ponto único de falha sem failover automático, e que o backend precisa ser alterado para conectar por JDBC direto em vez do conector do Cloud SQL.

## Contexto

O ZadInventory usa **MySQL** como banco de dados, acessado pelo backend Spring
Boot na porta 3306. Precisamos decidir **onde** esse banco vai rodar. A decisão
afeta segurança (exposição do dado), custo, isolamento de recursos e o esforço de
operação do grupo. O banco guarda dados de estoque e de usuários, então não pode
ficar exposto à internet nem competir por memória com a aplicação.

## Decisão

- A `vm-db` é uma `e2-small` (2 GB de RAM, disco `pd-balanced` de 20 GB), **sem IP
  externo**, na sub-rede privada `zad-privada`.
- A porta 3306 é liberada pela regra `allow-mysql` **apenas** para a tag
  `backend` — ou seja, só a `vm-backend` conversa com o banco.
- O banco fica isolado do frontend e do bastion, e invisível para a internet.

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| **Cloud SQL (serviço gerenciado)** | Traz backups automáticos, patching e alta disponibilidade prontos. Porém, com IP privado, ele fica numa rede do Google conectada por peering (Private Service Access, com outra faixa de IPs reservada), fora das nossas sub-redes e das regras de firewall por tag. Cobra por hora continuamente e não é destruído e recriado em minutos entre as sessões de teste. |
| **MySQL na mesma VM do backend** | Economiza uma VM, mas **acopla** banco e aplicação: os dois competem pela mesma memória, uma falha derruba ambos e não há isolamento de segurança. Além disso, uma `e2-micro` (1 GB) não comporta **JVM + MySQL** juntos; exigiria uma instância maior, reduzindo a economia. |

## Consequências

**Positivas:**
- Isolamento real entre banco e aplicação: cada um com sua memória e seu ciclo de
  vida.
- Banco inacessível pela internet; só o backend alcança a 3306.
- Controle total sobre versão, configuração e tuning do MySQL.

**Negativas (aceitas):**
- Ao abrir mão do Cloud SQL, o grupo assume **manualmente** tudo o que o serviço
  gerenciado faria: backups, restauração, patches de segurança e ajuste de desempenho.
- Há **uma só `vm-db` em uma só zona**: se a VM ou a zona cair, o sistema fica sem
  banco até o grupo recriar a máquina (via Terraform) e restaurar o backup, o que se
  reflete no RPO de 24 h e RTO de ~1 h assumidos na seção 5.1 do `arquitetura.md`.
- O backend precisa de uma alteração de código: trocar o conector do Cloud SQL
  (`spring-cloud-gcp-starter-sql-mysql`) por uma conexão JDBC direta a 10.10.2.20.
