# ADR-002: Saída para a internet da sub-rede privada

- **Status:** Aceito
- **Data:** 2026-09-30
- **Decisores:** Grupo 6 (Lipe, Eduardo)
- **Contexto do projeto:** ZadInventory na Google Cloud (`southamerica-east1`), Entrega 1 do Projeto Integrador

## Y-statement

No contexto da saída para a internet das VMs privadas do ZadInventory (`vm-backend` e `vm-db`, sem IP externo),
diante da necessidade de instalar e atualizar pacotes (sistema operacional, OpenJDK, MySQL) sem aceitar conexões vindas da internet,
decidimos usar o **Cloud NAT gerenciado**, associado a um Cloud Router e aplicado apenas à sub-rede `zad-privada`,
e descartamos fazer NAT em uma instância própria e simplesmente não ter saída para a internet,
para não manter um servidor de NAT nem quebrar a instalação automatizada,
aceitando que o Cloud NAT gera custo por hora mesmo sem tráfego (cerca de US$ 0,0078/h: US$ 0,0014 por VM mais US$ 0,005 pelo IP) e US$ 0,045 por GiB processado em ambas as direções, fora do nível gratuito, e que ele não filtra destinos: uma VM privada comprometida consegue enviar dados para qualquer endereço da internet.

## Contexto

As VMs da sub-rede privada `zad-privada` (`vm-backend` e `vm-db`) **não têm IP
externo**, por decisão de segurança. Mesmo assim, elas precisam de acesso de
**saída** à internet para tarefas essenciais de instalação e manutenção:

- `apt update` / `apt install` para atualizar o sistema operacional;
- instalar o **OpenJDK 17**, que executa o JAR do backend (o JAR é gerado fora da
  VM e copiado pelo bastion);
- adicionar o **repositório APT oficial do MySQL 8.4** e instalar o servidor.

Uma VM sem IP externo e sem NAT não consegue iniciar conexões para fora, o que
inviabilizaria a instalação automatizada da aplicação na Entrega 2.

## Decisão

- As VMs privadas iniciam conexões de saída; o Cloud NAT faz o SNAT (traduz o IP
  interno para um IP externo compartilhado do gateway).
- Nenhuma conexão **de entrada** iniciada pela internet é permitida por esse
  caminho — o NAT só responde ao tráfego que a própria VM iniciou.
- A sub-rede pública `zad-publica` **não** usa o NAT, porque suas VMs já têm IP
  externo próprio.

O Cloud NAT depende da rota `0.0.0.0/0 → default-internet-gateway` da VPC: ele não
é o "próximo salto" da rota — apenas traduz o endereço das VMs sem IP externo
sobre essa rota já existente.

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| **NAT em uma instância** (VM fazendo NAT por conta própria) | É mais barato em tráfego, mas vira um **ponto único de falha** que o grupo teria de manter, endurecer e dimensionar; exige configurar `ip_forward`, rotas e iptables manualmente. O enunciado também exige NAT gerenciado na Entrega 2. |
| **Não ter saída para a internet** | Zero custo de NAT e a opção mais segura, mas **inviabiliza a instalação**: backend e banco não conseguiriam baixar pacotes (JDK, repositório do MySQL) nem aplicar atualizações de segurança. Exigiria imagens pré-construídas, o que não cabe no prazo. |

## Consequências

**Positivas:**
- Serviço gerenciado pelo Google: sem VM extra para manter, escalar ou endurecer.
- Mantém as VMs privadas sem IP externo, preservando a postura de segurança.
- Permite instalação automatizada por Terraform + script de provisionamento.

**Negativas (aceitas):**
- O Cloud NAT **cobra mesmo parado**: há custo por hora de gateway e de IP externo,
  e US$ 0,045 por GiB processado, sem cobertura do free tier. Cada reinstalação de
  pacotes a cada `terraform apply` passa pelo NAT e é cobrada. Por isso o plano de
  controle de custos prevê `terraform destroy` ao fim de cada sessão de testes.
- O NAT não filtra destinos: a saída das VMs privadas fica liberada para qualquer
  endereço, e restringi-la exigiria regras de firewall de saída adicionais.
