# ZadInventory — Projeto Integrador (Grupo 6)

Sistema de controle de estoque (**Angular + Spring Boot + MySQL**) e o projeto da sua infraestrutura em **IaaS na Google Cloud**, para a disciplina de Arquitetura em Nuvem.

**Uniamérica Descomplica · Prof. Gildomiro Bairros**

## Integrantes

| Nome | RA | GitHub |
|---|---|---|
| Luiz Felipe Garbin Oliveira | 506064 | [@G4rb1n](https://github.com/G4rb1n) |
| Eduardo Gabriel Dreves Suptitz | 505212 | [@Eduol4](https://github.com/Eduol4) |

> A aplicação ZadInventory foi desenvolvida antes desta disciplina, com
> participação de Pedro Henrique Alves dos Santos, que não integra o grupo
> neste Projeto Integrador. Os commits a partir da Entrega 1 são dos
> integrantes listados acima.

## Visão geral

O ZadInventory controla produtos, categorias, tags e vendas de um pequeno comércio, com dois perfis de acesso (gerente e funcionário) e autenticação JWT.

A arquitetura projetada para a disciplina roda em `southamerica-east1`, dentro de uma VPC própria com duas sub-redes:

- **`zad-publica` (10.10.1.0/24):** `vm-bastion` (SSH administrativo) e `vm-frontend` (Nginx + Angular, único serviço público).
- **`zad-privada` (10.10.2.0/24):** `vm-backend` (Spring Boot) e `vm-db` (MySQL 8.4), sem IP externo e com saída pelo Cloud NAT.

## Documentação da Entrega 1

| Documento | Conteúdo |
|---|---|
| [`docs/arquitetura.md`](docs/arquitetura.md) | Aplicação, diagrama, endereçamento, rotas, segurança, tecnologias, dimensionamento, custos e riscos |
| [`docs/adr/`](docs/adr/) | `001-acesso-administrativo.md`, `002-saida-internet-subrede-privada.md`, `003-localizacao-banco.md` |
| [`docs/diagramas/`](docs/diagramas/) | `arquitetura.drawio` (fonte editável) e `arquitetura.png` |
| [`docs/custos/`](docs/custos/) | Estimativa exportada da Google Cloud Pricing Calculator |
| [`IA.md`](IA.md) | Declaração de uso de IA |
| [`infra/`](infra/) | Terraform (vazio nesta entrega; usado na Entrega 2) |

## Código da aplicação

| Pasta | Conteúdo |
|---|---|
| `zadinventory-backend/` | Spring Boot 3.5 (Java 17), projeto Maven em `zadinventory/` |
| `zadinventory-frontend/` | Angular 19 |
| `.github/workflows/` | CI: testes, SpotBugs/ESLint, Trivy/npm audit |

O deploy anterior em Cloud Run + Cloud SQL, e como rodar localmente, estão documentados em [`docs/implantacao-cloud-run.md`](docs/implantacao-cloud-run.md).
