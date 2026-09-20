package com.codeforge.colaboracion.config;

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
        // Prefijo para los mensajes que van desde el cliente hacia el servidor (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para los tópicos a los que los clientes se suscriben
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket con soporte SockJS para fallback y CORS permitido para Angular (http://localhost:4200)
        registry.addEndpoint("/ws-colaboracion")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint WebSocket nativo sin SockJS
        registry.addEndpoint("/ws-colaboracion")
                .setAllowedOriginPatterns("*");
    }
}
