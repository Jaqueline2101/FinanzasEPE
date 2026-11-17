# Etapa de compilación con Maven oficial y límite de memoria optimizado para Render
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos todo el repositorio
COPY . .

# Nos movemos al backend y compilamos limitando la memoria RAM a 256m para evitar que Render lo cancele
WORKDIR /app/finanzas-backend
RUN MAVEN_OPTS="-Xmx256m" mvn clean package -DskipTests

# Etapa de ejecución ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/finanzas-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]