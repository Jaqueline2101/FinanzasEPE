# Etapa de compilación usando Maven oficial
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos todo el contenido de la carpeta backend
COPY finanzas-backend/ .

# Compilamos directamente con el comando mvn del sistema (sin necesidad de mvnw)
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]