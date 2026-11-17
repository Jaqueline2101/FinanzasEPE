# Microservicio de Notificaciones Centralizado

Este microservicio actúa como un puente entre Apache Kafka y las aplicaciones frontend (Angular) mediante WebSockets con STOMP.

## Tecnologías

- Java 21
- Spring Boot 3.x
- Apache Kafka
- WebSockets (STOMP + SockJS)

## Requisitos

- Docker Desktop (para Kafka y Zookeeper)
- Maven 3.x

## Instalación y Ejecución

1. **Levantar Infraestructura**:

   ```bash
   docker-compose up -d
   ```

2. **Ejecutar el microservicio**:
   ```bash
   cd notificaciones-service
   mvn spring-boot:run
   ```

## Integración

- **Topic Kafka**: `sistema.notificaciones`
- **WebSocket Endpoint**: `http://localhost:8086/ws-notifications`
- **Suscripción Pública**: `/topic/notifications`
- **Suscripción Privada**: `/user/queue/notifications`

## Estructura del Mensaje (JSON)

```json
{
  "userId": "ID_DEL_USUARIO",
  "modulo": "Nombre del Módulo",
  "titulo": "Título de la Notificación",
  "contenido": { "any": "data" },
  "tipo": "INFO|SUCCESS|ERROR"
}
```
