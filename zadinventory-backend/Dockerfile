# ---------- Etapa 1: Build do JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copia o pom do projeto
COPY zadinventory/pom.xml .

# Baixa dependências para aproveitar cache
RUN mvn -B dependency:go-offline

# Copia o código fonte
COPY zadinventory/src ./src

# Gera o JAR
RUN mvn -B clean package -DskipTests


# ---------- Etapa 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /build/target/zadinventory-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]