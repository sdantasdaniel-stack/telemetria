package com.daniel.empresas.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmpresaNotificador {

    private final SimpMessagingTemplate messagingTemplate;

    public void notificar(Long empresaId, String tipo) {
    System.out.println(">>> EmpresaNotificador.notificar chamado: id=" + empresaId + " tipo=" + tipo);
    String payload = "{\"tipo\":\"" + tipo + "\",\"id\":" + empresaId + "}";
    messagingTemplate.convertAndSend("/topic/empresas", payload);
    System.out.println(">>> convertAndSend executado");
}
}