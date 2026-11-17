package com.pe.articulos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificationController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/enviar-test")
    public ApiResponse<String> sendTest(@RequestBody NotificationDTO notification) {
        // Para pruebas rápidas, este endpoint actúa como puente directo a Kafka
        kafkaTemplate.send("sistema.notificaciones", notification);
        return ApiResponse.success("Notificación enviada a Kafka para procesamiento");
    }
}
