# Etapa de compilación usando Maven oficial
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos todo el repositorio para mantener la estructura de carpetas
COPY . .

# Nos movemos a la carpeta del backend y compilamos desde ahí
WORKDIR /app/finanzas-backend
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el jar generado desde la subcarpeta del backend hacia la raíz de ejecución
COPY --from=build /app/finanzas-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]