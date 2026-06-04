package com.daniel.empresas.beans.admin;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.daniel.empresas.dto.request.DeviceRequestDTO;
import com.daniel.empresas.dto.response.DeviceResponseDTO;
import com.daniel.empresas.service.DeviceService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

//Para que a sessão fosse 100% stateless, nao poderia usar JSF primefaces, pois ele  guarda o estado da view na sessão.

@Component("adminDeviceBean")

//O bean vive enquanto você estiver na mesma página web
@ViewScoped
public class AdminDeviceBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Double latitude;
    private Double longitude;

    
    
    //É um @Component com @RequestScope — gerenciado pelo JSF junto com o Spring
    //O JSF instancia o bean primeiro, depois o Spring injeta as dependências
    //Por causa dessa "dupla gestão", o @RequiredArgsConstructor pode não funcionar corretamente
    //O @Autowired por campo é mais seguro nesse contexto
    @Autowired
    private DeviceService deviceService;

    private List<DeviceResponseDTO> devices;
    private DeviceResponseDTO deviceSelecionado;

    private String nome;
    private String identificador;
    private Long empresaId;
    
    // lista filtrada pelo PrimeFaces — necessária para o filtro da tabela funcionar
    private List<DeviceResponseDTO> devicesFiltrados;

    public List<DeviceResponseDTO> getDevicesFiltrados() { return devicesFiltrados; }
    public void setDevicesFiltrados(List<DeviceResponseDTO> devicesFiltrados) { this.devicesFiltrados = devicesFiltrados; }

    // carrega a lista de devices do banco
    public void carregar() {
    	
        devices = deviceService.listarTodos();
    }

    // cadastra um novo device com os dados do formulário
    public void cadastrar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return;
        }
        if (identificador == null || identificador.isBlank()) {
            addMensagemErro("O identificador é obrigatório");
            return;
        }
        if (empresaId == null) {
            addMensagemErro("O ID da empresa é obrigatório");
            return;
        }
        try {
        	DeviceRequestDTO dto = new DeviceRequestDTO(nome, identificador, empresaId, latitude, longitude);
            deviceService.cadastrar(dto);
            carregar();
            limparCampos();
            addMensagemSucesso("Device cadastrado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // desativa o device selecionado
    public void desativar(DeviceResponseDTO device) {
        try {
            deviceService.desativar(device.id());
            carregar();
            addMensagemSucesso("Device desativado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    private void limparCampos() {
        nome = null;
        identificador = null;
        empresaId = null;
        latitude = null;
        longitude = null;
        
    }

    private void addMensagemSucesso(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO, mensagem, null));
    }

    private void addMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, mensagem, null));
    }
    
    // deleta o device permanentemente
    public void deletar(DeviceResponseDTO device) {
        try {
            deviceService.deletar(device.id());
            carregar();
            addMensagemSucesso("Device deletado permanentemente");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }
    
    // reativa o device selecionado
    public void reativar(DeviceResponseDTO device) {
        try {
            deviceService.reativar(device.id());
            carregar();
            addMensagemSucesso("Device reativado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // getters e setters
    public List<DeviceResponseDTO> getDevices() { return devices; }
    public DeviceResponseDTO getDeviceSelecionado() { return deviceSelecionado; }
    public void setDeviceSelecionado(DeviceResponseDTO deviceSelecionado) { this.deviceSelecionado = deviceSelecionado; }
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