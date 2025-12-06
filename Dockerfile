# 1. Fase de Construcción (Build)
# Usamos una imagen de Maven con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Construimos el JAR saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# 2. Fase de Ejecución (Run)
# Usamos una imagen ligera de Java 21 para correr la app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el JAR generado en la fase anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone la app
EXPOSE 8080

# Comando de inicio estándar
# La configuración de la base de datos se tomará de application.properties
# y las variables de entorno de Render (DB_URL, DB_USERNAME, DB_PASSWORD)
ENTRYPOINT ["java", "-jar", "app.jar"]