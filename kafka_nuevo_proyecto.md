# Starter Kit: Kafka en un Nuevo Proyecto Spring Boot

Si vas a crear un proyecto nuevo y quieres que tenga este sistema de notificaciones, sigue estos 3 pasos:

## 1. Dependencia (pom.xml)
Añade el starter de Kafka de Spring Boot dentro de la sección `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## 2. Configuración (application.properties / yml)
Debes decirle a Spring dónde está tu servidor de Kafka y cómo enviar los mensajes. Añade esto a tu `application.properties`:

```properties
# Dirección del servidor Kafka (ajusta la IP si es necesario)
spring.kafka.bootstrap-servers=localhost:9092

# Configuración del Productor
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Opcional: ID del cliente
spring.kafka.producer.client-id=mi-nuevo-proyecto
```

## 3. El Servicio Productor (Copia este código)
Crea una clase llamada `KafkaProducerService` en tu proyecto. Este código ya incluye el manejo de JSON para que no tengas errores de serialización:

```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "sistema.notificaciones";

    public void enviarNotificacion(String userId, String modulo, String titulo, Object contenido, String tipo) {
        Map<String, Object> message = Map.of(
            "userId", userId != null ? userId : "",
            "modulo", modulo,
            "titulo", titulo,
            "contenido", contenido != null ? contenido : "",
            "tipo", tipo
        );
        
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, json);
            log.info("Notificación enviada: {}", json);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar notificación", e);
        }
    }
}
```

## 4. Infraestructura (Docker)
Recuerda que para que el código funcione, necesitas que Kafka esté corriendo. Puedes usar este `docker-compose.yml` básico:

```yaml
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```
