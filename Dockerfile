# ==========================================
# Etapa 1: Compilación de Backend con Maven y Java 21
# (Los archivos estáticos de Angular ya están en src/main/resources/static/)
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY finanzas-backend/ ./finanzas-backend/

WORKDIR /app/finanzas-backend
ENV MAVEN_OPTS="-Xmx1024m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen Ligera de Ejecución con Java 21 JRE Alpine
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/finanzas-backend/target/*.jar app.jar

EXPOSE 10000
EXPOSE 8080

# Control de memoria JVM optimizado para el plan gratuito de Render (512MB RAM)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UseSerialGC", "-Xms64m", "-Xmx200m", "-XX:MaxMetaspaceSize=200m", "-XX:ReservedCodeCacheSize=48m", "-Xss512k", "-jar", "app.jar"]
