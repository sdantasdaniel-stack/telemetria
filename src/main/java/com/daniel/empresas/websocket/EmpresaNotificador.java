package com.daniel.empresas.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmpresaNotificador {

    private final SimpMessagingTemplate messagingTemplate;

    public void notificar() {
        System.out.println(">>> notificar() chamado");
        System.out.println(">>> Notificador template hash: " + System.identityHashCode(messagingTemplate));
        messagingTemplate.convertAndSend("/topic/empresas", "atualizar");
        System.out.println(">>> convertAndSend executado");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmpresaAtualizada(EmpresaAtualizadaEvent event) {
        System.out.println(">>> listener chamado");
        messagingTemplate.convertAndSend("/topic/empresas", "atualizar");
        System.out.println(">>> convertAndSend executado no listener");
    }
}