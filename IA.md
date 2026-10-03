# Declaração de uso de Inteligência Artificial

Este documento declara o uso de ferramentas de IA na Entrega 1 do Projeto
Integrador (Grupo 6 — ZadInventory na Google Cloud), conforme exigido pelo
enunciado. O uso de IA foi de **apoio**: todo o conteúdo foi revisado, ajustado e
verificado pelos integrantes antes de ir para o repositório.

## Ferramentas utilizadas

| Integrante | Ferramenta | Onde foi usada | O que a IA produziu |
|---|---|---|---|
| Lipe | Claude (chat) | Documentação da arquitetura | Rascunhos do `arquitetura.md`, dos READMEs, do material de custos, a primeira versão dos 3 ADRs e do diagrama `.drawio`, e revisão final dos ADRs e do diagrama |
| Lipe | Claude Code | Repositório | Apoio na organização da estrutura de pastas e dos arquivos do projeto |
| Eduardo | Claude (chat) | ADRs, diagrama e esta declaração | Reescrita e revisão dos 3 ADRs (`docs/adr/`), apoio na edição do diagrama, rascunho do `IA.md` e preparação para a defesa técnica |

## O que foi verificado e corrigido pelo grupo

- **Valores de custo conferidos na calculadora oficial.** Os números dos cenários
  A e B foram validados na Google Cloud Pricing Calculator, e não apenas aceitos
  do rascunho gerado pela IA.
- **Preço dos IPs externos corrigido.** O rascunho trazia US$ 0,005/h; ao conferir
  na calculadora, o grupo ajustou para **US$ 0,0025/h**, valor usado na estimativa
  final.
- **Decisões dos ADRs revisadas.** Cada decisão, alternativa descartada e
  consequência aceita foi lida e validada pelos integrantes, para que ambos
  consigam defendê-las oralmente (o professor sorteia quem responde).
- **Justificativas dos ADRs corrigidas.** Na revisão, removemos justificativas
  inadequadas que a IA havia sugerido (como "sem ganho para a nota") e corrigimos
  detalhes inconsistentes com a arquitetura, por exemplo a referência à seção do
  RPO/RTO e a instalação de dependências do Spring Boot nas VMs, que não acontece
  porque o JAR é gerado fora delas.
- **Links indevidos removidos do diagrama.** O arquivo `.drawio` continha links
  externos sem relação com o projeto embutidos nos textos; eles foram removidos
  antes do commit.
- **Consistência entre documentos.** CIDRs, nomes e tipos das VMs, tags de
  firewall e os fluxos citados nos ADRs foram conferidos contra o `arquitetura.md`.

## Declaração

As ferramentas de IA foram usadas como apoio à redação e à organização do
material. As decisões técnicas de arquitetura, os valores de custo e as
justificativas são de responsabilidade do grupo, que revisou e validou todo o
conteúdo entregue.