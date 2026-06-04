package com.daniel.empresas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.daniel.empresas.dto.response.DeviceResponseDTO;
import com.daniel.empresas.service.DeviceService;



@Component("mapaBean")
@SessionScope
public class MapaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private DeviceService deviceService;

    // chave lida do application.properties
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    // lista de devices que serão exibidos no mapa
    private List<DeviceResponseDTO> devices = new ArrayList<>();

    // carregado ao entrar na página
    public void carregar() {
        devices = deviceService.listarParaMapa();
    }

    // gera o JSON com os markers para o JavaScript do mapa ler
    // formato: [{"id":1,"nome":"Device A","lat":-8.05,"lng":-34.88,"status":"ONLINE"}, ...]
    public String getDevicesJson() {
        if (devices == null || devices.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < devices.size(); i++) {
            DeviceResponseDTO d = devices.get(i);
            sb.append("{")
              .append("\"id\":").append(d.id()).append(",")
              .append("\"nome\":\"").append(escapar(d.nome())).append("\",")
              .append("\"identificador\":\"").append(escapar(d.identificador())).append("\",")
              .append("\"lat\":").append(d.latitude()).append(",")
              .append("\"lng\":").append(d.longitude()).append(",")
              .append("\"status\":\"").append(d.status() != null ? d.status().name() : "DESCONHECIDO").append("\"")
              .append("}");
            if (i < devices.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // escapa caracteres especiais para não quebrar o JSON
    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String getGoogleMapsApiKey() { return googleMapsApiKey; }
    public List<DeviceResponseDTO> getDevices() { return devices; }
}