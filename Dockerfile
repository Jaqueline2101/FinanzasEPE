FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY . .

WORKDIR /app/finanzas-backend
# Añadimos -e para ver el error exacto en los logs de Render
RUN mvn clean package -DskipTests -e

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/finanzas-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]