# ADR-001: Estratégia de acesso administrativo

- **Status:** Aceito
- **Data:** 2026-09-30
- **Decisores:** Grupo 6 (Lipe, Eduardo)
- **Contexto do projeto:** ZadInventory na Google Cloud (`southamerica-east1`), Entrega 1 do Projeto Integrador

## Y-statement

No contexto do acesso administrativo às VMs privadas do ZadInventory (`vm-backend` e `vm-db`, sem IP externo),
diante da necessidade de administrá-las sem expô-las à internet, de nunca liberar SSH para `0.0.0.0/0` e de demonstrar SSH funcional na Entrega 2,
decidimos usar um **bastion host** (`vm-bastion`, 10.10.1.10) com SSH restrito aos IPs /32 dos integrantes e acesso às VMs internas por ProxyJump,
e descartamos dar IP público com SSH a cada VM e usar o IAP (Identity-Aware Proxy) do Google,
para reduzir a superfície de ataque a uma única porta 22 exposta e manter o controle explícito de quem acessa,
aceitando que o bastion se torna um ponto único de acesso administrativo (se ele cair, ninguém entra nas VMs internas), que pagamos uma VM e um IP externo a mais só para isso, e que o grupo precisará atualizar manualmente os IPs /32 sempre que o IP residencial de um integrante mudar, sob risco de perder o próprio acesso.

## Contexto

As VMs de backend (`vm-backend`) e de banco (`vm-db`) ficam na sub-rede privada
`zad-privada` (10.10.2.0/24) e **não têm IP externo**. Ainda assim, o grupo
precisa acessá-las por SSH para instalar pacotes, aplicar configuração e depurar
problemas durante a Entrega 2.

O enunciado exige que a matriz de segurança **nunca** libere SSH (porta 22) para
`0.0.0.0/0`, e que a Entrega 2 demonstre um acesso SSH funcional.

## Decisão

- A regra de firewall `allow-ssh-admin` libera a porta 22 do bastion **apenas
  para os IPs /32 de cada integrante do grupo** — nunca para faixas amplas.
- As VMs internas recebem SSH somente a partir do bastion, pela regra
  `allow-ssh-interno` (origem = tag `bastion`).
- O acesso às VMs privadas é feito por **SSH ProxyJump** (`ssh -J`), saltando
  pelo bastion, sem que elas jamais tenham IP público.

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| **IP público em cada VM, com SSH aberto** | Multiplica a superfície de ataque: cada VM passa a ser alvo direto de varreduras e ataques de força bruta na porta 22, e ainda gera custo de IP externo por VM. Contraria o princípio de menor privilégio. |
| **IAP (Identity-Aware Proxy) do Google** | Dispensaria o bastion e o IP externo, e autentica por IAM. Porém a origem liberada no firewall seria a faixa do Google (35.235.240.0/20), e não os IPs do grupo, e o grupo não tem experiência com o encaminhamento TCP do IAP para configurá-lo e depurá-lo no prazo da Entrega 2. |

## Consequências

**Positivas:**
- Uma única porta de entrada administrativa, mais fácil de auditar e endurecer.
- As VMs internas ficam inacessíveis pela internet — só existem dentro da VPC.
- Atende diretamente à regra de "SSH nunca para 0.0.0.0/0".

**Negativas (aceitas):**
- O bastion é um **ponto único de acesso administrativo**: se a `vm-bastion` cair
  ou for mal configurada, o grupo perde o SSH para backend e banco.
- Como os IPs residenciais são dinâmicos, um integrante pode ficar trancado do lado
  de fora até atualizar a regra `allow-ssh-admin` com seu novo IP /32.
- Custo de uma VM (`e2-micro`) e de um IP externo a mais, só para acesso administrativo.
