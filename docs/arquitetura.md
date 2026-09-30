# Arquitetura do ZadInventory na Google Cloud — Entrega 1

> Projeto Integrador · Uniamérica Descomplica · Prof. Gildomiro Bairros · **Grupo 6**
>
> Este documento é o **projeto** da infraestrutura que será implantada via Terraform na Entrega 2 (22/11/2026). Nada aqui precisa estar rodando agora. Os ADRs estão em [`adr/`](adr/), o diagrama em [`diagramas/`](diagramas/) e a estimativa de custos em [`custos/`](custos/).

---

## 5.1 Descrição da aplicação

### Problema e usuários

O **ZadInventory** é um sistema de controle de estoque para pequenos comércios. Ele substitui planilhas e anotações manuais no controle de produtos, entradas e vendas, que geram divergência entre o estoque real e o registrado.

| Perfil | Role na aplicação | O que faz |
|---|---|---|
| Gerente (administrador) | `ROLE_GERENTE` | Cadastra usuários, produtos, categorias e tags; consulta relatórios de vendas |
| Funcionário (limitado) | `ROLE_FUNCIONARIO` | Registra operações (vendas/movimentações) e consulta produtos |

### Funcionalidades

- Login com JWT próprio (`POST /api/auth/login`) e criação do primeiro gerente (`POST /api/usuarios/criar-inicial`).
- CRUD de produtos (`/api/produtos`), incluindo busca por nome, por categoria e alerta de **baixo estoque**.
- CRUD de categorias (`/api/categorias`) e tags (`/api/tags`).
- Registro de operações/vendas (`/api/operacoes`), mudança de situação e relatórios de total de vendas por produto.
- Gestão de usuários (`/api/usuarios`), restrita ao gerente.

**Comportamento verificável com persistência:** cadastrar um produto pela interface, recarregar a página e vê-lo listado; registrar uma venda e ver o total de vendas e o estoque atualizados.

### Componentes técnicos

| Componente | Tecnologia | Exposição |
|---|---|---|
| Frontend | Angular 19 (build estático) servido por Nginx, que também faz proxy reverso de `/api/` | **Público** (portas 80/443) |
| Backend (API) | Spring Boot 3.5.16, Java 17, JAR com Tomcat embutido na porta 8080 | **Privado** — só recebe tráfego do frontend |
| Banco de dados | MySQL 8.4 LTS na porta 3306 | **Privado** — só recebe tráfego do backend |
| Bastion | VM de salto para SSH administrativo | Pública, mas SSH só a partir dos IPs do grupo |

> **Situação atual × arquitetura desta disciplina.** Hoje o ZadInventory roda em Cloud Run + Cloud SQL (ver `docs/implantacao-cloud-run.md`). Para esta disciplina ele será reimplantado em **IaaS**: VMs Compute Engine em uma VPC própria, com bastion e Cloud NAT. Duas adaptações de código serão feitas na Entrega 2: (1) um perfil Spring `vm` com JDBC direto (`jdbc:mysql://10.10.2.20:3306/zadinventory`) no lugar do conector do Cloud SQL; (2) o *auth-proxy* em Go do frontend deixa de ser necessário, porque o Nginx passa a encaminhar `/api/` direto para o IP privado do backend (o isolamento passa a ser feito por firewall, não por IAM do Cloud Run).

### Requisitos não-funcionais

| Requisito | Valor | Justificativa |
|---|---|---|
| Usuários simultâneos | até **20** | Um comércio pequeno: 1–2 gerentes e alguns funcionários em turno |
| Tempo de resposta | < 1 s nas operações de CRUD | Uso interativo em balcão |
| Disponibilidade | Horário comercial, **sem alta disponibilidade** | Custo pago pelo grupo; ver abaixo |
| RPO / RTO | 24 h / ~1 h | Backup diário (Entrega 2) e recriação via `terraform apply` + restore |
| Segurança | Senhas com BCrypt, JWT, backend e banco sem IP externo | Dados de vendas e usuários |

**Esta arquitetura NÃO oferece alta disponibilidade.** Há uma única VM por função, todas na zona `southamerica-east1-a`. Se a zona, a VM do frontend, do backend ou do banco cair, o sistema fica indisponível até a VM ser recriada. Essa escolha é deliberada, por custo, e está detalhada em [5.10 Riscos](#510-riscos-e-limitações).

---

## 5.2 Diagrama

Arquivos:

- Fonte editável: [`diagramas/arquitetura.drawio`](diagramas/arquitetura.drawio) (abrir em <https://app.diagrams.net>)
- Imagem: [`diagramas/arquitetura.png`](diagramas/arquitetura.png)

![Arquitetura ZadInventory na GCP](diagramas/arquitetura.png)

### Como a GCP representa "pública" e "privada"

Na GCP, a **VPC é global** e as **sub-redes são regionais**. Diferente da AWS, uma sub-rede não é pública ou privada por si só: não existe tabela de rotas por sub-rede. A segmentação deste projeto vem de três mecanismos combinados:

1. **IP externo:** só `vm-bastion` e `vm-frontend` (sub-rede `zad-publica`) recebem IP externo.
2. **Cloud NAT:** configurado **apenas** para a sub-rede `zad-privada`, cujas VMs não têm IP externo.
3. **Regras de firewall por tag de rede:** definem quem fala com quem (seção 5.5).

Por isso chamamos `zad-publica` de "pública" e `zad-privada` de "privada": o nome descreve a política aplicada, não uma propriedade nativa da sub-rede.

### Fluxos numerados

| Fluxo | Caminho |
|---|---|
| **(1) Usuário acessando a aplicação** | Navegador → HTTPS 443 → IP externo da `vm-frontend` (Nginx) → arquivos do Angular. Chamadas `/api/*` → Nginx faz proxy para `10.10.2.10:8080` (`vm-backend`) → backend consulta `10.10.2.20:3306` (`vm-db`) → resposta volta pelo mesmo caminho |
| **(2) Administrador via SSH** | Máquina do integrante (IP /32 liberado) → SSH 22 → IP externo da `vm-bastion` → SSH 22 (ProxyJump) → IP interno da VM de destino (`10.10.1.20`, `10.10.2.10` ou `10.10.2.20`) |
| **(3) Instância privada saindo para a internet** | `vm-backend` ou `vm-db` (sem IP externo) → rota `0.0.0.0/0` → Cloud NAT `zad-nat` traduz para o IP externo do NAT → internet (ex.: `apt update`, `repo.mysql.com`). As respostas voltam pelo NAT; conexões iniciadas **de fora** não entram |

Comando de exemplo do fluxo 2:

```bash
# BASTION_IP = IP externo do bastion (muda a cada recriação)
ssh -J usuario@$BASTION_IP usuario@10.10.2.10
```

---

## 5.3 Endereçamento IP

| Rede / sub-rede | CIDR | Região / zona | Tipo | Finalidade | IPs utilizáveis |
|---|---|---|---|---|---|
| `zad-vpc` (bloco de planejamento) | 10.10.0.0/16 | global | VPC custom | Reserva de endereçamento do projeto | — |
| `zad-publica` | 10.10.1.0/24 | southamerica-east1 (VMs em `-a`) | Pública (VMs com IP externo) | `vm-bastion`, `vm-frontend` | 252 |
| `zad-privada` | 10.10.2.0/24 | southamerica-east1 (VMs em `-a`) | Privada (sem IP externo, saída via NAT) | `vm-backend`, `vm-db` | 252 |
| *livre (futuro)* | 10.10.3.0/24 – 10.10.255.0/24 | — | — | Sub-rede de HA/gerência na Entrega 2, se necessário | — |

**IPs fixos das VMs (IP interno estático reservado):**

| VM | IP interno | IP externo |
|---|---|---|
| vm-bastion | 10.10.1.10 | Efêmero |
| vm-frontend | 10.10.1.20 | Estático (o domínio aponta para ele) |
| vm-backend | 10.10.2.10 | Nenhum |
| vm-db | 10.10.2.20 | Nenhum |

Todas as faixas estão em **10.0.0.0/8 (RFC 1918)** e não se sobrepõem. A faixa também não colide com a rede doméstica típica dos integrantes (192.168.0.0/16).

**A VPC não tem CIDR próprio na GCP.** Em modo custom, só as sub-redes têm faixas. O bloco 10.10.0.0/16 é uma convenção nossa para garantir que qualquer sub-rede futura fique dentro dele e não se sobreponha.

**IPs reservados pela GCP.** Em cada faixa primária a GCP reserva **4 endereços**. Em `zad-privada`, por exemplo:

| Endereço | Uso |
|---|---|
| 10.10.2.0 | Endereço de rede |
| 10.10.2.1 | Gateway padrão da sub-rede |
| 10.10.2.254 | Reservado pela GCP (penúltimo) |
| 10.10.2.255 | Broadcast |

Sobram 256 − 4 = **252 IPs utilizáveis** por sub-rede.

**Por que /24 se usamos só 2 IPs em cada sub-rede?** A GCP permite **expandir** uma sub-rede depois, mas não **reduzir**. Um /28 (12 IPs utilizáveis) bastaria hoje. Mas na Entrega 2, que cobra HA, pode ser necessário um grupo de instâncias com várias VMs de frontend/backend. O /24 dá essa folga sem custo nenhum (endereço privado não é cobrado), mantém a leitura simples (o terceiro octeto identifica a sub-rede) e ainda deixa 253 blocos /24 livres no /16.

---

## 5.4 Tabelas de rota

Na GCP as rotas pertencem à **VPC** e valem para todas as sub-redes. Não existe tabela por sub-rede como na AWS.

| Destino | Próximo salto | Prioridade | Criada por | Vale para |
|---|---|---|---|---|
| 10.10.1.0/24 | Rede `zad-vpc` (rota de sub-rede) | — | Automática ao criar a sub-rede | Toda a VPC |
| 10.10.2.0/24 | Rede `zad-vpc` (rota de sub-rede) | — | Automática ao criar a sub-rede | Toda a VPC |
| 0.0.0.0/0 | `default-internet-gateway` | 1000 | Automática na criação da VPC (mantida no Terraform) | Toda a VPC |

Como o enunciado pede a tabela **por sub-rede**, abaixo está a rota efetiva que cada uma enxerga e o alvo real do tráfego:

**Sub-rede `zad-publica` (10.10.1.0/24)**

| Destino | Alvo | Observação |
|---|---|---|
| 10.10.1.0/24 | Local (rota de sub-rede) | Tráfego dentro da própria sub-rede |
| 10.10.2.0/24 | Local (rota de sub-rede) | Frontend → backend; bastion → VMs privadas |
| 0.0.0.0/0 | `default-internet-gateway` | Sai pelo **IP externo da própria VM** (bastion e frontend) |

**Sub-rede `zad-privada` (10.10.2.0/24)**

| Destino | Alvo | Observação |
|---|---|---|
| 10.10.2.0/24 | Local (rota de sub-rede) | Backend → banco |
| 10.10.1.0/24 | Local (rota de sub-rede) | Respostas ao frontend e ao bastion |
| 0.0.0.0/0 | `default-internet-gateway`, com tradução pelo **Cloud NAT `zad-nat`** | As VMs não têm IP externo; o NAT troca o IP de origem pelo IP do NAT |

**O Cloud NAT não é um próximo salto de rota.** Ele não aparece na tabela: a tradução acontece na camada de rede definida por software da GCP, para VMs sem IP externo das sub-redes associadas ao NAT. O equivalente conceitual ao "0.0.0.0/0 → NAT" da AWS é a combinação *rota padrão para o internet gateway + Cloud NAT associado a `zad-privada`*.

**O que acontece sem essa rota ou sem o NAT:**

- **Sem a rota `0.0.0.0/0`:** nenhuma VM alcança a internet. O Cloud NAT também deixa de funcionar, porque depende da rota para o internet gateway. O frontend continua respondendo a conexões que chegam, mas as VMs privadas não conseguem instalar pacotes (`apt`), baixar o MySQL nem aplicar atualizações de segurança. O bastion também não teria como responder a quem se conecta de fora.
- **Com a rota, mas sem Cloud NAT:** as VMs públicas funcionam normalmente. Os pacotes de `vm-backend` e `vm-db` para a internet são **descartados**, porque essas VMs não têm IP externo. O `startup-script` que instala JDK e MySQL falha, e a aplicação não sobe.
- **Rotas de sub-rede:** não podem ser removidas enquanto a sub-rede existir. Sem elas, frontend → backend → banco não se comunicariam.

---

## 5.5 Matriz de segurança

Na GCP as regras de firewall são da VPC e aplicadas **por instância através de tags de rede** (`target_tags`). Usar `source_tags` como origem é o equivalente a "referenciar outro security group" na AWS: a regra acompanha a função da VM, não o IP.

Regras implícitas da GCP, que não podem ser apagadas: **nega toda entrada** (prioridade 65535) e **permite toda saída** (prioridade 65535). Portanto, tudo o que não estiver liberado abaixo está bloqueado na entrada.

| Regra | Grupo (tag alvo) | Direção | Protocolo | Porta | Origem / destino | Justificativa |
|---|---|---|---|---|---|---|
| `allow-web` | `frontend` | Entrada | TCP | 80, 443 | 0.0.0.0/0 | Único serviço público. 443 serve a aplicação com TLS; 80 só redireciona para 443 e atende o desafio HTTP do Let's Encrypt |
| `allow-ssh-admin` | `bastion` | Entrada | TCP | 22 | **IP /32 de cada integrante**, definido na variável `admin_ips` do Terraform (não publicada no repositório) | Acesso administrativo só a partir de máquinas conhecidas. **Nunca 0.0.0.0/0** |
| `allow-ssh-interno` | `frontend`, `backend`, `db` | Entrada | TCP | 22 | Tag `bastion` | Só o bastion abre SSH nas demais VMs (ProxyJump) |
| `allow-api` | `backend` | Entrada | TCP | 8080 | Tag `frontend` | Só o Nginx do frontend chama a API; a internet não alcança o backend |
| `allow-mysql` | `db` | Entrada | TCP | 3306 | Tag `backend` | Só a aplicação acessa o banco; frontend e bastion não |
| *(implícita)* | todas | Entrada | todos | todas | 0.0.0.0/0 | **Negado** — qualquer outra entrada é bloqueada |
| *(implícita)* | todas | Saída | todos | todas | 0.0.0.0/0 | Permitido. Backend e banco precisam sair via NAT para `apt` e repositórios; o frontend, para renovar o certificado |

**Camadas adicionais além do firewall:**

- **Sem IP externo** em `vm-backend` e `vm-db`: mesmo com um erro de firewall, elas não são alcançáveis a partir da internet.
- **MySQL** configurado com `bind-address = 10.10.2.20` e usuário da aplicação criado como `'zad'@'10.10.2.10'`, ou seja, restrito ao IP do backend.
- **SSH** só com chaves (autenticação por senha desativada), gerenciado por **OS Login**: acesso concedido e revogado por papel IAM (`roles/compute.osLogin`), sem copiar chaves entre máquinas.
- **Segredos** (`DB_PASSWORD`, `JWT_SECRET`) num arquivo de ambiente do systemd com permissão `600` na `vm-backend`, fora do Git.

**Por que tags e não service accounts nas regras?** Filtrar por service account é mais forte, porque quem pode editar uma VM consegue trocar a tag dela. Como só os integrantes têm permissão de edição no projeto, as tags bastam e deixam as regras mais legíveis. Essa limitação está registrada em 5.10.

---

## 5.6 Tecnologias

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| Provedor | Google Cloud Platform | — | VPC com sub-redes, Cloud NAT gerenciado, IP externo e firewall por instância, tudo com suporte no Terraform. O grupo já tem conta e projeto na GCP |
| Região | `southamerica-east1` (São Paulo), zona `-a` | — | Ver justificativa abaixo |
| Sistema operacional | Ubuntu Server LTS (imagem `ubuntu-2404-lts-amd64`) | 24.04 LTS | Suporte até 2029, pacotes de Nginx, OpenJDK e certbot no repositório oficial, e familiaridade do grupo |
| Runtime do backend | OpenJDK JRE headless | 17 | Mesma versão usada no build (`java.version=17` no `pom.xml`) e no CI |
| Aplicação backend | Spring Boot (JAR com Tomcat embutido) | 3.5.16 | Versão atual do projeto, com CVEs críticas corrigidas |
| Aplicação frontend | Angular (build de produção estático) | 19.2 | Versão atual do projeto |
| Servidor web / proxy reverso | Nginx | 1.24 (Ubuntu 24.04) | Serve os estáticos do Angular e encaminha `/api/` para o backend privado. Leve, cabe numa e2-micro |
| TLS | Certbot + Let's Encrypt, com subdomínio DuckDNS | certbot 2.x | Certificado gratuito. Sem TLS, senhas e JWT trafegariam em texto claro |
| Banco de dados | MySQL Community Server (repositório oficial APT) | 8.4 LTS | O projeto já usa MySQL (`mysql-connector-j`). A 8.0 chegou ao fim de vida em abril de 2026; a 8.4 é a LTS atual |
| IaC | Terraform (ou OpenTofu, compatível) | ≥ 1.9 | Provisiona VPC, sub-redes, firewall, Router/NAT e VMs de forma reproduzível: essencial para destruir e recriar entre testes |
| Provider IaC | `hashicorp/google` | ~> 7.0 (fixar a última no `required_providers`) | Provider oficial da GCP |
| Instalação da aplicação | `startup-script` (metadados da VM) + systemd | — | O Terraform injeta o script que instala pacotes. O JAR e o `dist/` do Angular são copiados via `scp` pelo bastion e rodam como serviço systemd (`zadinventory.service`) e site Nginx |
| Controle de versão | Git + GitHub | — | Repositório do grupo, com CI já existente (testes, SpotBugs, Trivy) |

### Justificativa de provedor e região

| Critério | `southamerica-east1` (escolhida) | `us-east1` / `us-central1` |
|---|---|---|
| **Latência** a partir de Foz do Iguaçu | Baixa (~20–30 ms) | ~150 ms |
| **Custo** | Mais cara por hora; fora do free tier | Mais barata; 1 e2-micro grátis no Always Free |
| **Disponibilidade de serviços** | Compute Engine, Cloud NAT, IP estático, snapshots de disco e 3 zonas disponíveis: tudo o que a Entrega 2 exige (implantação, HA em mais de uma zona e backup) | Também disponíveis |
| **Dados** | Ficam no Brasil (alinhado à LGPD para dados de clientes/vendas) | Fora do país |

Escolhemos São Paulo por **latência** e **localização dos dados**. O free tier cobriria só **uma** das quatro VMs, então a economia real da região americana seria pequena. No cenário B (seção 5.9) pagamos apenas ~100 horas por VM, então a diferença de preço por hora entre as regiões resulta em poucos dólares no total. O deploy atual em Cloud Run também já está nessa região.

---

## 5.7 Dimensionamento

| Componente | Família | Tipo | vCPU | Memória | Disco (tipo e tamanho) | Sub-rede | Justificativa (resumo) |
|---|---|---|---|---|---|---|---|
| Bastion (`vm-bastion`) | E2 (uso geral, CPU compartilhada) | `e2-micro` | 2 compartilhadas (0,25 sustentada) | 1 GB | `pd-standard` 10 GB | zad-publica | Só repassa SSH; é o menor tipo E2 |
| Frontend (`vm-frontend`) | E2 (CPU compartilhada) | `e2-micro` | 2 compartilhadas (0,25 sustentada) | 1 GB | `pd-standard` 10 GB | zad-publica | Nginx com estáticos usa < 100 MB; build feito fora da VM |
| Aplicação (`vm-backend`) | E2 (CPU compartilhada) | `e2-small` | 2 compartilhadas (0,5 sustentada) | 2 GB | `pd-balanced` 20 GB | zad-privada | JVM do Spring Boot precisa de 400–600 MB; 1 GB causaria OOM |
| Banco de dados (`vm-db`) | E2 (CPU compartilhada) | `e2-small` | 2 compartilhadas (0,5 sustentada) | 2 GB | `pd-balanced` 20 GB | zad-privada | Buffer pool de 768 MB mantém o banco em memória |

### Justificativas

**vm-bastion (e2-micro).** Só repassa conexões SSH, então usa CPU e memória quase nulas. É o menor tipo E2; não existe tamanho menor útil (a antiga `f1-micro` é de geração anterior e não tem vantagem de preço). Disco `pd-standard` de 10 GB, o mínimo da imagem Ubuntu, porque não há I/O relevante.

**vm-frontend (e2-micro).** O Nginx servindo arquivos estáticos e fazendo proxy usa menos de 100 MB de RAM para 20 usuários. O build do Angular é feito fora da VM (na máquina do integrante ou no CI), então ela não precisa de memória para compilar. É o menor tipo disponível.

**vm-backend (e2-small), por que não e2-micro?** Uma JVM com Spring Boot, Hibernate e Spring Security ocupa de 400 a 600 MB de RSS logo após subir. Com 1 GB total e o sistema operacional ocupando ~250 MB, a VM ficaria sem folga e o kernel mataria o processo (OOM killer) sob carga. Com 2 GB, limitamos o heap a `-Xmx1g` e sobra memória para o sistema. Usamos `pd-balanced` porque o JAR e os logs se beneficiam de I/O melhor, a um custo baixo em 20 GB.

**vm-db (e2-small), por que não e2-micro?** O MySQL 8.4 reserva ~128 MB para o *buffer pool* do InnoDB e algumas centenas de MB para o `performance_schema` e buffers por conexão. Em 1 GB ele até sobe, mas o *buffer pool* teria de ser reduzido, e mais consultas iriam ao disco. Com 2 GB configuramos `innodb_buffer_pool_size=768M`, suficiente para manter o banco inteiro em memória (algumas dezenas de MB de dados). Usamos `pd-balanced` porque banco é sensível a latência de disco.

**Por que não e2-medium (4 GB)?** Para 20 usuários simultâneos não há necessidade. O custo por hora dobraria sem ganho perceptível.

### O que acontece ao atingir o limite de CPU compartilhada

As VMs `e2-micro` e `e2-small` têm **núcleo compartilhado**: garantem uma fração sustentada da CPU (0,25 e 0,5 vCPU) e podem usar as 2 vCPUs em **rajadas** curtas enquanto houver créditos acumulados. Quando o uso fica acima da fração sustentada por tempo prolongado:

- A VM **não é desligada nem cobrada a mais**. Ela sofre **throttling**: fica limitada à fração sustentada.
- **Efeito no ZadInventory:** respostas mais lentas, principalmente na inicialização do Spring Boot (que é intensiva em CPU e pode passar de 1 minuto) e em relatórios de vendas.
- **Mitigação:** acompanhar a métrica de utilização de CPU no Cloud Monitoring. Se o throttling for constante no backend, subir para `e2-medium` (1 vCPU sustentada) é só mudar o `machine_type` no Terraform.

---

## 5.9 Estimativa de custos

> Valores da **Google Cloud Pricing Calculator**, com preços vigentes em 29/09/2026. A exportação (CSV) e os links ficam em [`custos/`](custos/).

**Link da calculadora — cenário A:** <https://cloud.google.com/calculator?dl=CjhDaVJqTVRjMFpXWmxOQzFpTkRRM0xUUTFOekF0T0dFMk55MDJORFE0TWprMU9ERTNZak1RQVE9PRokMjk0QzgxQUItRTBGQS00NkExLUExRUYtRjYxNkMxNEIwMEVD>

**Link da calculadora — cenário B:** <https://cloud.google.com/products/calculator?dl=CjhDaVEwTldObE9HRTNPUzFtTlRRMExUUTBOV1V0T1dabE1DMDBOakZoWTJNM1pXWTJNRGtRQVE9PRAIGiRDQjk3NDhBMS0wOUQyLTQ4NUQtOERDRS1DQTA2MDk0NDZDMUU>

### Itens de custo

| Item | Qtd. | Base de cobrança |
|---|---|---|
| VM e2-micro (bastion, frontend) | 2 | Por hora ligada |
| VM e2-small (backend, db) | 2 | Por hora ligada |
| Disco `pd-standard` 10 GB | 2 | Por GB-mês, enquanto o disco **existir**, mesmo com a VM parada |
| Disco `pd-balanced` 20 GB | 2 | Por GB-mês, enquanto existir |
| IP externo em VM (bastion + frontend) | 2 | US$ 0,0025/h cada |
| Cloud NAT — gateway | 1 (2 VMs usando) | US$ 0,0014 por VM/h |
| Cloud NAT — IP externo do NAT | 1 | US$ 0,005/h |
| Cloud NAT — dados processados | ~2 GiB/mês | US$ 0,045/GiB (entrada e saída) |
| Egress para a internet | ~2 GiB/mês | ~US$ 0,19/GiB a partir de São Paulo |

### Cenário A — operação contínua (730 h/mês, 24×7)

| Item | Custo mensal |
|---|---|
| 2× e2-micro (vCPU + RAM) | US$ 19,41 |
| 2× e2-small (vCPU + RAM) | US$ 38,83 |
| Discos: 2× 10 GB standard + 2× 20 GB balanced | US$ 7,20 |
| 2 IPs externos das VMs | US$ 3,65 |
| Cloud NAT: gateway (2 VMs) | US$ 2,04 |
| Cloud NAT: IP do gateway | US$ 3,65 |
| Cloud NAT: dados processados (2 GiB) | US$ 0,09 |
| Egress para a internet (2 GiB, São Paulo → América do Sul) | US$ 0,38 |
| **Total** | **US$ 75,25** |

### Cenário B — só as horas ligadas na Entrega 2 (o que o grupo vai pagar)

Premissa de uso: **~100 horas por VM** entre outubro e 23/11, ou seja, cerca de **22 sessões de trabalho de ~4 h** (implantação, testes de segurança, HA e backup), mais **~12 h** no dia da apresentação, com o ambiente subido no início da sessão e destruído ao final. A premissa é propositalmente folgada: o grupo ainda está aprendendo Terraform, e subestimar as horas seria subestimar o que vamos pagar.

A calculadora foi usada com as VMs em 100 h cada. Dois ajustes foram feitos à mão, porque a calculadora não permite informar horas nesses itens:

- **IP Address e NAT Gateway** não têm campo de horas e são sempre cotados como mês cheio (730 h). Como o ambiente é destruído entre as sessões, IPs e NAT só existem nas mesmas ~100 h das VMs, então usamos **valor mensal da calculadora × 100 / 730**.
- **Dados processados pelo NAT e egress** são cobrados por volume, não por hora, então ficam iguais aos da calculadora.

| Item | Na calculadora | Custo considerado |
|---|---|---|
| 2× e2-micro, 100 h cada (vCPU + RAM) | US$ 2,66 | US$ 2,66 |
| 2× e2-small, 100 h cada (vCPU + RAM) | US$ 5,32 | US$ 5,32 |
| Discos (standard + balanced) | US$ 3,60 | US$ 3,60 |
| 2 IPs externos das VMs | US$ 3,65 (mês cheio) | US$ 0,50 (3,65 × 100/730) |
| Cloud NAT: gateway + IP | US$ 5,69 (mês cheio) | US$ 0,78 (5,69 × 100/730) |
| Cloud NAT: dados processados (2 GiB) | US$ 0,09 | US$ 0,09 |
| Egress para a internet (2 GiB) | US$ 0,38 | US$ 0,38 |
| **Total** | **US$ 21,39** | **US$ 13,33** |

O valor que o grupo espera pagar é **~US$ 13,33**. O total bruto da calculadora, US$ 21,39, é o teto: é quanto pagaríamos se, por erro, IPs e NAT ficassem ligados o mês inteiro sem as VMs. O plano de controle abaixo existe para evitar exatamente isso.

### Item mais caro e como reduzir

O item mais caro é o **par de VMs e2-small** (backend e banco): US$ 44,83/mês com os discos, cerca de **60% do total** do cenário A, e US$ 8,32 (~62%) do cenário B. O Cloud NAT, que costuma ser o vilão, fica em US$ 5,78/mês aqui porque só 2 VMs o usam e o tráfego é pequeno.

| Redução possível | Economia | O que se perde |
|---|---|---|
| Juntar backend e banco numa única e2-medium | 1 VM a menos e 1 VM a menos no NAT | Isolamento entre aplicação e banco. A regra `allow-mysql` perde sentido, e uma falha na aplicação derruba o banco junto |
| Rebaixar o banco para e2-micro com swap | Parte do custo de uma VM | Desempenho: *buffer pool* menor e risco de lentidão/OOM |
| Mudar para `us-east1` | Preço menor e 1 e2-micro grátis | Latência (~150 ms) e dados fora do Brasil |
| Ligar só durante os testes (**adotado**) | Paga ~100 h por VM em vez de 730 h | O ambiente não fica disponível entre as sessões; os dados de teste são recriados a cada subida |

### Uso de nível gratuito

- **Always Free (e2-micro): não aproveitado.** Ele só vale em `us-west1`, `us-central1` e `us-east1`, e escolhemos `southamerica-east1` (ver 5.6).
- **Crédito de avaliação da GCP: não disponível.** A conta de faturamento do grupo está em teste gratuito, mas o crédito depende de um pré-pagamento que não foi feito, e nenhum crédito aparece em Faturamento → Créditos (conferido em 29/09/2026). A instituição também não oferece créditos. Portanto, **todo o custo do cenário B será pago pelo grupo**, e o plano de controle de custos abaixo é o que garante que o valor fique perto do estimado.

### Estratégia de recriação: custo × esforço

Destruir o ambiente entre sessões reduz o custo, mas obriga a reinstalar a aplicação a cada recriação. **Nossa estratégia é automatizar a instalação** para poder destruir sempre:

- O **Terraform** cria rede, firewall, NAT e VMs, e injeta um `startup-script` em cada VM que instala os pacotes (Nginx, OpenJDK, MySQL), cria o banco e o usuário da aplicação e configura o serviço systemd.
- Um script `infra/scripts/deploy.sh` copia o JAR e o build do Angular pelo bastion, inicia os serviços e roda o *seed* SQL com dados de teste.
- Meta: ambiente do zero até a aplicação respondendo em **menos de 20 minutos**, com dois comandos (`terraform apply` e `deploy.sh`).

O custo dessa escolha é o esforço inicial: escrever e testar os scripts nas primeiras sessões da Entrega 2, que por isso serão mais longas. Até a automação estar estável, a instalação manual será documentada passo a passo. A única exceção à regra de destruir é o dia da apresentação, quando o ambiente fica ligado o dia inteiro.

### Plano de controle de custos

1. **Alerta de orçamento** no Billing: orçamento de **US$ 20** para o projeto, com avisos por e-mail a todos os integrantes em 50%, 90% e 100%.
2. **Destruir e recriar o ambiente entre testes:** `terraform apply` no início da sessão e `terraform destroy` ao final (estratégia detalhada abaixo).
3. **Labels em todos os recursos** (`projeto=zadinventory`, `entrega=2`, `grupo=6`, `ambiente=teste`) para filtrar o relatório de faturamento.
4. **Projeto GCP dedicado** para a disciplina, separado do projeto onde roda o Cloud Run atual.
5. **Desligar o que já está ativo hoje:** o Cloud SQL e o Serverless VPC Access Connector do deploy atual cobram por hora mesmo sem uso. Eles serão parados ou removidos enquanto não forem necessários.
6. **Conferência semanal** do relatório de faturamento por um integrante responsável, em rodízio.

---

## 5.10 Riscos e limitações

| # | Ponto único de falha / limitação | Impacto | Mitigação (atual ou na Entrega 2) |
|---|---|---|---|
| 1 | **vm-db é uma única VM**, sem réplica | Se a VM ou o disco falharem, o sistema para e os dados desde o último backup se perdem | Backup diário (snapshot do disco + `mysqldump`) na Entrega 2; RPO de 24 h |
| 2 | **Zona única** (`southamerica-east1-a`) | Uma falha zonal derruba as 4 VMs ao mesmo tempo | Aceito por custo. A Entrega 2 pode distribuir frontend/backend em duas zonas |
| 3 | **vm-frontend é a única porta de entrada** (sem load balancer) | Se ela cair, ninguém acessa a aplicação, mesmo com backend e banco funcionando | Recriação rápida via Terraform com o IP externo estático; LB como evolução para HA |
| 4 | **vm-bastion é ponto único do acesso administrativo** | Sem bastion, não há SSH para as VMs privadas (a aplicação continua funcionando) | Recriação via Terraform; o acesso pelo console de série da GCP como emergência |
| 5 | **IPs residenciais dinâmicos** na regra `allow-ssh-admin` | Quando o IP de um integrante muda, ele fica sem SSH até a regra ser atualizada | Variável `admin_ips` no Terraform; atualizar e aplicar antes de cada sessão |
| 6 | **CPU compartilhada** (E2) | Sob carga contínua, a VM é limitada à fração sustentada: o sistema fica lento, mas não para | Monitorar CPU; subir para e2-medium se necessário |
| 7 | **Saída para a internet irrestrita** via NAT | Uma VM comprometida pode enviar dados para qualquer destino | Evolução: regras de saída restringindo destinos aos repositórios de pacotes |
| 8 | **Firewall por tag** | Quem tem permissão de editar a VM pode trocar a tag e ganhar acesso | Só integrantes têm papel de edição; evolução: firewall por service account |
| 9 | **Destruir o ambiente apaga os dados** | Dados de teste se perdem entre sessões | Script de *seed*; na apresentação o ambiente fica de pé o dia todo |
| 10 | **TLS depende do DuckDNS e do Let's Encrypt** | Se a emissão falhar, a aplicação só responde em HTTP (credenciais em texto claro) | Emitir o certificado na primeira sessão de testes, com tempo para corrigir |
