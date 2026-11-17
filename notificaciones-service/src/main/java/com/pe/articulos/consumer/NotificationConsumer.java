package com.pe.articulos.consumer;

import com.pe.articulos.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "sistema.notificaciones", groupId = "notificaciones-group")
    public void consume(NotificationDTO notification) {
        log.info("Recibiendo notificación de Kafka: {}", notification);

        if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
            // Envío privado a un usuario específico
            // El cliente debe estar suscrito a /user/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId(), 
                    "/queue/notifications", 
                    notification
            );
            log.info("Notificación enviada al usuario: {}", notification.getUserId());
        } else {
            // Envío público (broadcast)
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("Notificación enviada a todos (broadcast)");
        }
    }
}
