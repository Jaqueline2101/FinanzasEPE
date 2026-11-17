package com.pe.articulos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria para enviar mensajes a los clientes
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Prefijo para los mensajes que van desde el cliente hacia el servidor (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para los mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint que usará Angular para conectarse con SockJS (Legacy)
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        // Endpoint para WebSockets Nativos (el que usa el frontend actualmente)
        registry.addEndpoint("/ws-notifications-native")
                .setAllowedOriginPatterns("*");
    }
}
