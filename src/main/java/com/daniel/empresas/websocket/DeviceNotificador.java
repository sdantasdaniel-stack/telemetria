package com.daniel.empresas.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.daniel.empresas.model.StatusEnum;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeviceNotificador {

    private final SimpMessagingTemplate messagingTemplate;

    // chamado quando status muda (ONLINE/OFFLINE) — envia id e status para atualizar o pin
    public void notificar(Long deviceId, StatusEnum status) {
        String payload = "{\"id\":" + deviceId + ",\"status\":\"" + status.name() + "\"}";
        messagingTemplate.convertAndSend("/topic/devices", payload);
    }
    
    // chamado quando device é desativado, reativado ou deletado — manda reload simples
    public void notificarReload() {
        System.out.println(">>> DeviceNotificador.notificarReload() chamado");
        messagingTemplate.convertAndSend("/topic/devices", "reload");
        System.out.println(">>> DeviceNotificador.notificarReload() executado");
    }
}