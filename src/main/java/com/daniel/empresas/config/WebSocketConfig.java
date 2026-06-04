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
        // prefixo dos tópicos que o cliente vai assinar
        config.enableSimpleBroker("/topic");
        // prefixo das mensagens que o cliente envia para o servidor (não uso por enquanto)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint de conexão WebSocket — o PrimeFaces vai conectar aqui
        registry.addEndpoint("/ws").withSockJS();
    }
    
    
}