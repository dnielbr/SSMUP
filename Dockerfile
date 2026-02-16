# =========================================
# STAGE 1 - Build da aplicação
# =========================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copia arquivos do Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa dependências (cache de layer)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copia código fonte
COPY src src

# Gera o JAR
RUN ./mvnw clean package -DskipTests -B


# =========================================
# STAGE 2 - Runtime (produção)
# =========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuário não-root
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

USER spring:spring

# Copia o jar gerado
COPY --from=build /app/target/*.jar app.jar

# Porta padrão (Render sobrescreve com PORT)
EXPOSE 8080

# =========================================
# JVM OTIMIZADA PARA RENDER FREE (512MB)
# =========================================
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx384m", \
  "-XX:+UseContainerSupport", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]