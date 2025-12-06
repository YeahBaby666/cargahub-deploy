# 1. Fase de Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Fase de Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# TRUCO: Reemplazamos 'postgres://' por 'jdbc:postgresql://' al vuelo
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.datasource.url=${DB_URL/postgres:/jdbc:postgresql:}"]