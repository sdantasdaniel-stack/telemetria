package com.daniel.empresas.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.daniel.empresas.model.StatusEnum;

import lombok.RequiredArgsConstructor;

import com.daniel.empresas.model.Device;

@Component
@RequiredArgsConstructor
public class DeviceNotificador {

    private final SimpMessagingTemplate messagingTemplate;

    // status mudou (ONLINE/OFFLINE) — atualiza só o ícone e infoWindow
    public void notificar(Long deviceId, StatusEnum status) {
        String payload = "{\"tipo\":\"status\",\"id\":" + deviceId 
            + ",\"status\":\"" + status.name() + "\"}";
        messagingTemplate.convertAndSend("/topic/devices", payload);
    }

    // device foi editado — atualiza nome, coords, identificador e status no mapa
    public void notificarAtualizacao(Device device) {
        String payload = "{\"tipo\":\"editado\","
            + "\"id\":" + device.getId() + ","
            + "\"nome\":\"" + escapar(device.getNome()) + "\","
            + "\"identificador\":\"" + escapar(device.getIdentificador()) + "\","
            + "\"status\":\"" + device.getStatus().name() + "\","
            + "\"ativo\":" + device.isAtivo() + ","
            + "\"lat\":" + (device.getLatitude()  != null ? device.getLatitude()  : "null") + ","
            + "\"lng\":" + (device.getLongitude() != null ? device.getLongitude() : "null")
            + "}";
        messagingTemplate.convertAndSend("/topic/devices", payload);
    }

    // device desativado, reativado ou deletado — remove/esconde o pin
    public void notificarEstado(Long deviceId, String tipo) {
        // tipo: "desativado" | "reativado" | "deletado"
        String payload = "{\"tipo\":\"" + tipo + "\",\"id\":" + deviceId + "}";
        messagingTemplate.convertAndSend("/topic/devices", payload);
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}