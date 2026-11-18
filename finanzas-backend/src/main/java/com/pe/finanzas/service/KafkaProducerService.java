package com.pe.finanzas.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class KafkaProducerService {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOPIC = "sistema.notificaciones";

    public void enviarNotificacion(String userId, String modulo, String titulo, Object contenido, String tipo) {
        if (kafkaTemplate == null) {
            log.debug("Kafka no está habilitado, omitiendo notificación.");
            return;
        }
        Map<String, Object> message = Map.of(
                "userId", userId != null ? userId : "",
                "modulo", modulo,
                "titulo", titulo,
                "contenido", contenido != null ? contenido : "",
                "tipo", tipo);

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // Ejecutamos el envío de forma asíncrona para no bloquear el hilo principal/transacción
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    log.info("Enviando notificación a Kafka (Asíncrono): {}", jsonMessage);
                    kafkaTemplate.send(TOPIC, jsonMessage);
                } catch (Exception e) {
                    log.error("Error en el hilo de envío a Kafka", e);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Error al serializar mensaje de Kafka", e);
        }
    }
}
