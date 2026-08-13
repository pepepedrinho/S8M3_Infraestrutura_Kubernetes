# ZAD Inventory

Sistema composto por **Frontend** e **Backend**, executados separadamente no Google Cloud Platform através do **Cloud Run**.

## Deploy manual

### 1. Autenticação no GCP

Faça login:

```bash
gcloud auth login
```

Defina o projeto:

```bash
gcloud config set project project-bb29153a-91af-47dd-8df
```

Configure o Docker para utilizar o Artifact Registry:

```bash
gcloud auth configure-docker southamerica-east1-docker.pkg.dev
```

---

## 2. Frontend

Crie a imagem na pasta do frontend:

```bash
docker build -t frontend:1.x .
```

Adicione a tag do Artifact Registry:

```bash
docker tag frontend:1.x southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x
```

Envie a imagem:

```bash
docker push southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x
```

Após o `push`, o deploy pode ser realizado pela interface do **Cloud Run**, selecionando a imagem enviada ao Artifact Registry.

Ou diretamente pelo `gcloud`:

```bash
gcloud run deploy frontend \
  --image=southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/frontend:1.x \
  --region=southamerica-east1 \
  --platform=managed
```

---

## 3. Backend

Crie a imagem na pasta do backend:

```bash
docker build -t backend:1.x .
```

Adicione a tag do Artifact Registry:

```bash
docker tag backend:1.x southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.x
```

Envie a imagem:

```bash
docker push southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.x
```

Após o `push`, o deploy pode ser realizado pela interface do **Cloud Run**, configurando as variáveis de ambiente, VPC Connector e demais configurações necessárias.

Ou diretamente pelo `gcloud`:

```bash
gcloud run deploy backend \
  --image=southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.0 \
  --region=southamerica-east1 \
  --platform=managed
```

---

## 4. Novas versões

Para publicar uma nova versão, altere a tag da imagem:

```bash
docker build -t backend:1.y .
docker tag backend:1.y southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.y
docker push southamerica-east1-docker.pkg.dev/project-bb29153a-91af-47dd-8df/zadinventory/backend:1.y
```

Depois, faça o deploy da nova imagem no Cloud Run.

O mesmo processo se aplica ao frontend.

> **Importante:** não versionar senhas, tokens ou outras credenciais no repositório.