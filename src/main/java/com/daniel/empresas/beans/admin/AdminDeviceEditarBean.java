package com.daniel.empresas.beans.admin;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.daniel.empresas.dto.request.DeviceRequestDTO;
import com.daniel.empresas.service.DeviceService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Bean de edição de dispositivos no painel administrativo.
 * Carrega os dados de um device existente e permite atualizar suas informações.
 *
 * Escopo de sessão mantido para preservar o estado entre requisições JSF
 * durante o fluxo de edição.
 */

@Component("adminDeviceEditarBean")

@SessionScope
public class AdminDeviceEditarBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double latitude;
    private Double longitude;

    @Autowired
    private DeviceService deviceService;

    private Long id;
    private String nome;
    private String identificador;
    private Long empresaId;

    public void carregar() {
    	 if (id != null) {
             var device = deviceService.buscarPorId(id);
             this.nome = device.nome();
             this.identificador = device.identificador();
             this.empresaId = device.empresaId();
             this.latitude = device.latitude();
             this.longitude = device.longitude();
         }
    }

    public String atualizar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return null;
        }
        if (identificador == null || identificador.isBlank()) {
            addMensagemErro("O identificador é obrigatório");
            return null;
        }
        if (empresaId == null) {
            addMensagemErro("O ID da empresa é obrigatório");
            return null;
        }
        try {
            DeviceRequestDTO dto = new DeviceRequestDTO(nome, identificador, empresaId, latitude, longitude);
            deviceService.atualizar(id, dto);
            return "/pages/admin/devices.xhtml?faces-redirect=true";
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
            return null;
        }
    }

    private void addMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, mensagem, null));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}