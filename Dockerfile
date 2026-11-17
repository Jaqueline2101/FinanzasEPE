# ==========================================
# Etapa 1: Compilación con Maven y Java 21
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos únicamente el código del backend para mantener la imagen limpia
COPY finanzas-backend/ ./finanzas-backend/

# Compilamos el backend empaquetando el JAR ejecutable
WORKDIR /app/finanzas-backend
ENV MAVEN_OPTS="-Xmx1024m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen ligera de ejecución con Java 21 JRE Alpine
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la etapa de compilación
COPY --from=build /app/finanzas-backend/target/*.jar app.jar

# Render usa el puerto 10000 por defecto para servicios web
EXPOSE 10000
EXPOSE 8080

# Control de memoria JVM optimizado para el plan gratuito de Render (512MB RAM)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UseSerialGC", "-Xms64m", "-Xmx200m", "-XX:MaxMetaspaceSize=200m", "-XX:ReservedCodeCacheSize=48m", "-Xss512k", "-jar", "app.jar"]
