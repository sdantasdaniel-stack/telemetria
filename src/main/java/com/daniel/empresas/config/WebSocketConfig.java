package com.daniel.empresas.config;

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
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    // setAllowedOrigins adicionado para o Angular — o SockJS tem validação de origem própria,
    // separada do CORS configurado no SecurityConfig. Mesmo com o CORS liberado para
    // localhost:4200, o handshake WebSocket seria rejeitado sem essa linha porque o SockJS
    // verifica a origem do upgrade HTTP independentemente do Spring Security.
    // Em produção trocar pelo domínio real do Angular.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }
}