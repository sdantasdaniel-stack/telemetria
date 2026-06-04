package com.daniel.empresas.beans.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.daniel.empresas.dto.request.DeviceRequestDTO;
import com.daniel.empresas.dto.response.DeviceResponseDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.UsuarioRepository;
import com.daniel.empresas.security.JwtService;
import com.daniel.empresas.service.DeviceService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component("userDeviceBean")
@ViewScoped
public class UserDeviceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    // lista de empresas do usuário logado
    private List<EmpresaResponseDTO> empresasDoUsuario = new ArrayList<>();

    // empresa selecionada pelo usuário
    private Long empresaIdSelecionada;

    // lista de devices da empresa selecionada
    private List<DeviceResponseDTO> devices = new ArrayList<>();

    // campos do formulário de cadastro
    private String nome;
    private String identificador;

    // coordenadas opcionais — devices sem coordenadas não aparecem no mapa
    private Double latitude;
    private Double longitude;

    // carrega as empresas do usuário logado ao entrar na página
    // também chamado pelo WebSocket quando uma empresa é desativada ou deletada
    public void carregar() {
    	empresasDoUsuario = getEmpresasDoUsuarioLogado();

    	// verifica se a empresa atualmente selecionada ainda está na lista de empresas ativas
    	// se não estiver — foi desativada ou deletada — limpa a seleção e a lista de devices
    	if (empresaIdSelecionada != null) {
    		boolean empresaAindaAtiva = empresasDoUsuario.stream()
    				.anyMatch(e -> e.id().equals(empresaIdSelecionada));
    		if (!empresaAindaAtiva) {
    			empresaIdSelecionada = null;
    			devices = new ArrayList<>();
         }
     }
 }

    // carregado quando o usuário seleciona uma empresa no dropdown
    public void selecionarEmpresa() {
        if (empresaIdSelecionada != null) {
            // usa o método filtrado — devices inativos não aparecem para o usuário
            devices = deviceService.listarAtivosPorEmpresa(empresaIdSelecionada);
        }
    }

    // cadastra um novo device na empresa selecionada
    public void cadastrar() {
        if (empresaIdSelecionada == null) {
            addMensagemErro("Selecione uma empresa primeiro");
            return;
        }
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return;
        }
        if (identificador == null || identificador.isBlank()) {
            addMensagemErro("O identificador é obrigatório");
            return;
        }
        try {
            DeviceRequestDTO dto = new DeviceRequestDTO(nome, identificador, empresaIdSelecionada, latitude, longitude);
            deviceService.cadastrar(dto);

            // recarrega a lista de devices após cadastrar
            devices = deviceService.listarAtivosPorEmpresa(empresaIdSelecionada);
            limparCampos();
            addMensagemSucesso("Device cadastrado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    public String getEmpresasDoUsuarioJson() {
    if (empresasDoUsuario == null || empresasDoUsuario.isEmpty()) return "[]";
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < empresasDoUsuario.size(); i++) {
        sb.append(empresasDoUsuario.get(i).id());
        if (i < empresasDoUsuario.size() - 1) sb.append(",");
    }
    sb.append("]");
    return sb.toString();
    }

    public void carregarDevices() {
    if (empresaIdSelecionada != null) {
        devices = deviceService.listarAtivosPorEmpresa(empresaIdSelecionada);
    }
    }

    // busca as empresas do usuário logado lendo o token JWT do cookie
    private List<EmpresaResponseDTO> getEmpresasDoUsuarioLogado() {
        HttpServletRequest httpRequest = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt-token".equals(cookie.getName())) {
                    String email = jwtService.validarToken(cookie.getValue());
                    if (email != null) {
                        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
                        if (usuario != null && usuario.getEmpresas() != null) {
                            return usuario.getEmpresas().stream()
                                    // filtra apenas empresas ativas — inativas não aparecem para o usuário
                                    .filter(e -> e.isAtivo())
                                    .map(e -> new EmpresaResponseDTO(
                                            e.getId(),
                                            e.getNome(),
                                            e.getCnpj(),
                                            e.getEmail(),
                                            e.isAtivo()))
                                    .toList();
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private void limparCampos() {
        nome = null;
        identificador = null;
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

    public List<EmpresaResponseDTO> getEmpresasDoUsuario() { return empresasDoUsuario; }
    public Long getEmpresaIdSelecionada() { return empresaIdSelecionada; }
    public void setEmpresaIdSelecionada(Long empresaIdSelecionada) { this.empresaIdSelecionada = empresaIdSelecionada; }
    public List<DeviceResponseDTO> getDevices() { return devices; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}