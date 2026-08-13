# ZAD Inventory

Sistema de inventário composto por **Backend** (Spring Boot) e **Frontend** (Angular), publicados como serviços independentes no **Google Cloud Run**.

## Estrutura do repositório

```
zadinventory-backend/          Dockerfile do backend
  └── zadinventory/            projeto Maven (pom.xml, mvnw, src)
zadinventory-frontend/         projeto Angular + Dockerfile + proxy Go
.github/workflows/             pipelines de CI/CD
```

> Os dois caminhos do backend são diferentes de propósito: o Maven roda em `zadinventory-backend/zadinventory`, enquanto o `docker build` usa `zadinventory-backend` como contexto, porque o Dockerfile faz `COPY zadinventory/pom.xml` relativo a ele.

---

## Deploy automático (GitHub Actions)

É o caminho normal de publicação. Cada serviço tem seu próprio workflow, disparado apenas quando a sua pasta muda:

| Workflow | Dispara quando muda | Testa | Publica |
|---|---|---|---|
| `backend.yml` | `zadinventory-backend/**` | `mvn -B verify` no Java 17 | Cloud Run `backend` |
| `frontend.yml` | `zadinventory-frontend/**` | `npm ci` + `npm run build` no Node 22 | Cloud Run `frontend` |

**Os testes rodam em push e em pull request. O deploy só acontece em push para `main`.** Num PR o job de publicação aparece como `skipped`, então dá para validar a pipeline inteira sem publicar nada.

Como os filtros são por pasta, um commit que mexe só no frontend não reconstrói o backend.

### Identificação das imagens

Cada imagem recebe como tag o **SHA completo do commit**, e é a mesma tag no build, no push e no deploy — o que sobe é exatamente o que foi construído:

```
southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:<sha>
southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:<sha>
```

Não existe tag `latest`. Para descobrir o que está publicado, veja o SHA na revisão do Cloud Run e cole no GitHub para chegar ao commit exato. No Artifact Registry, ordene por data de envio — o nome não é sequencial.

### Configuração necessária

Secrets do repositório, usados na autenticação por Workload Identity:

| Secret | Para quê |
|---|---|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | provedor de identidade federada |
| `GCP_SERVICE_ACCOUNT` | conta de serviço que executa o deploy |

> **Atenção:** o `gcloud run deploy` do workflow passa apenas a imagem, a região e a service account. As variáveis de ambiente, a conexão com o Cloud SQL e o VPC Connector **não** estão no repositório — vivem na configuração do serviço no console, e o `gcloud run deploy` as preserva ao trocar só a imagem.
>
> A consequência prática é que o deploy automático **depende do serviço já existir e estar configurado**. Se o serviço `backend` for recriado do zero, ou for publicado numa região nova, ele sobe sem as variáveis e o contêiner não inicia. Nesse caso, configure o serviço pelo console antes (veja o deploy manual abaixo) ou acrescente `--set-env-vars`, `--update-secrets` e `--add-cloudsql-instances` ao workflow.

---

## Deploy manual

Necessário na primeira publicação de um serviço, quando ainda não há o que preservar, e útil como alternativa se o Actions estiver indisponível.

### 1. Autenticação no GCP

```bash
gcloud auth login
gcloud config set project project-bb29153a-91af-47dd-8df
gcloud auth configure-docker southamerica-east1-docker.pkg.dev
```

### 2. Frontend

Na pasta `zadinventory-frontend`:

```bash
docker build -t frontend:1.x .
docker tag frontend:1.x southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x
docker push southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x
```

O deploy pode ser feito pela interface do Cloud Run, selecionando a imagem enviada, ou pelo `gcloud`:

```bash
gcloud run deploy frontend \
  --image=southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x \
  --region=southamerica-east1 \
  --platform=managed
```

### 3. Backend

Na pasta `zadinventory-backend`:

```bash
docker build -t backend:1.x .
docker tag backend:1.x southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.x
docker push southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.x
```

No deploy do backend é preciso configurar as variáveis de ambiente, o VPC Connector e a conexão com o Cloud SQL. Em produção o serviço exige `INSTANCE_CONNECTION_NAME`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` e `JWT_SECRET` — sem qualquer uma delas o contêiner não inicia.

```bash
gcloud run deploy backend \
  --image=southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.x \
  --region=southamerica-east1 \
  --platform=managed
```

> As tags `1.x` são a convenção do fluxo manual. O registry contém as duas convenções: `1.x` das publicações manuais e SHAs de commit das automáticas.

---

## Desenvolvimento local

O passo a passo do backend (MySQL local, perfil `local`, criação do primeiro usuário, autenticação) está em [`zadinventory-backend/COMO-RODAR-LOCAL.md`](zadinventory-backend/COMO-RODAR-LOCAL.md).

Frontend:

```bash
cd zadinventory-frontend
npm install
npm start        # ng serve com proxy.conf.json
```

## Testes

```bash
cd zadinventory-backend/zadinventory
mvn test         # 93 testes unitários (Surefire)
mvn verify       # + 50 testes de integração (Failsafe) e relatório JaCoCo
```

A separação é por diretório: `src/test/java/**/unit/` roda no Surefire e `src/test/java/**/Integration/` no Failsafe. Como o CI precisa das duas suítes, o workflow usa `verify`, não `test`.

Nenhum teste precisa de MySQL — todos usam H2 em memória através do perfil `test`.

---

> **Importante:** não versionar senhas, tokens ou outras credenciais no repositório. O `application-local.properties`, que guarda a senha do banco de desenvolvimento, está no `.gitignore` por esse motivo.
