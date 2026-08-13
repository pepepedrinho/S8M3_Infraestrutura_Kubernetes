# ZadInventory Backend — rodando no VSCode

Spring Boot 3.5.4 · Java 17 · MySQL · JWT

## Pré-requisitos (já instalados nesta máquina)

| Item | Versão detectada |
|---|---|
| JDK | 21.0.5 (`C:\Program Files\Java\jdk-21`) — compila em nível 17, ok |
| Maven | 3.9.11 |
| MySQL | 8.0 (serviço `MySQL80`, porta 3306) |

Extensões do VSCode recomendadas (o próprio editor sugere ao abrir a pasta):
`vscjava.vscode-java-pack` e `vmware.vscode-boot-dev-pack`.

## 1. Configurar o acesso ao banco

Edite `zadinventory/src/main/resources/application-local.properties` e informe a
senha do seu MySQL:

```properties
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:SUA_SENHA_AQUI}
```

O banco `zadinventory` é criado automaticamente na primeira execução
(`createDatabaseIfNotExist=true`), e o Hibernate cria as tabelas (`ddl-auto=update`).

> Este arquivo está no `.gitignore` justamente porque guarda a sua senha.

## 2. Rodar

**Pelo VSCode:** aba *Run and Debug* → **ZadInventory (local)** → F5.
(A configuração já está em `.vscode/launch.json`, com o perfil `local` ativo.)

**Pelo terminal:**

```powershell
cd C:\zadinventory-backend\zadinventory
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

A API sobe em `http://localhost:8080`.

## 3. Criar o primeiro usuário e fazer login

O endpoint `/api/usuarios/criar-inicial` é público e só funciona enquanto não
existir nenhum usuário:

```powershell
curl -X POST http://localhost:8080/api/usuarios/criar-inicial `
  -H "Content-Type: application/json" `
  -d '{\"nome\":\"Admin\",\"email\":\"admin@zad.com\",\"senha\":\"123456\",\"tipoUsuario\":\"GERENTE\"}'

curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@zad.com\",\"senha\":\"123456\"}'
```

**Atenção:** o token JWT vai no header `X-App-Authorization`, **não** em
`Authorization` (esse fica reservado ao Google ID Token do Cloud Run):

```
X-App-Authorization: Bearer <token>
```

## 4. Testes

```powershell
mvn test              # 129 testes unitários + de controller
mvn verify            # inclui o relatório JaCoCo em target/jacoco-report
```

## Perfis

| Perfil | Banco | Quando |
|---|---|---|
| *(nenhum)* | Google Cloud SQL | produção / Cloud Run — exige `INSTANCE_CONNECTION_NAME`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` |
| `local` | MySQL em `localhost:3306` | desenvolvimento |
| `test` | H2 em memória | testes automatizados |

## Observação sobre o Avast

O Avast intercepta HTTPS e o Java não confia no certificado dele, o que fazia o
Maven falhar com `PKIX path building failed` ao baixar dependências. A variável
de ambiente de usuário `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`
foi configurada para o Java usar o repositório de certificados do Windows.
O mesmo valor está em `.vscode/settings.json` para o servidor de linguagem Java.
