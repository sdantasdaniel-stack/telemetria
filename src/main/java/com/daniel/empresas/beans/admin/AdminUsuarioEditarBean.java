package com.daniel.empresas.beans.admin;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.context.annotation.SessionScope;

import com.daniel.empresas.dto.request.UsuarioRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.model.RoleEnum;
import com.daniel.empresas.service.EmpresaService;
import com.daniel.empresas.service.UsuarioService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;



@Component("adminUsuarioEditarBean")
//@SessionScope — o Bean vive durante toda a sessão do usuario

@SessionScope
public class AdminUsuarioEditarBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmpresaService empresaService;

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private RoleEnum role;
    private List<Long> empresasIds = new ArrayList<>();
    private List<EmpresaResponseDTO> empresasDisponiveis;

    public void carregar() {
        empresasDisponiveis = empresaService.listarTodas();
        if (id != null) {
            var usuario = usuarioService.buscarPorId(id);
            this.nome = usuario.nome();
            this.email = usuario.email();
            this.role = usuario.role();
            this.empresasIds = new ArrayList<>(usuario.empresasIds());
                    
        }
    }
    
    // verifica se o email tem formato mínimo válido
    // exige pelo menos um caractere antes do @ e pelo menos um depois
    private boolean emailValido(String email) {
    	// garante que email não é nulo ou espaços em brancos
    	if (email == null || email.isBlank()) return false;
    		int arroba = email.indexOf('@');
    		// O método indexOf() retorna -1 por padrão de projeto do Java para indicar que o caractere não foi encontrado e o return volta falso
    		// arroba deve existir, ter pelo menos 1 caractere antes e pelo menos 1 depois
    		// arroba > 0: Garante que o @ não é o primeiro caractere, ou seja, exige que haja pelo menos uma letra antes dele
    		//arroba < email.length() - 1: Garante que o @ não é o último caractere, exigindo que haja pelo menos uma letra ou domínio depois dele
    		return arroba > 0 && arroba < email.length() - 1;
    		
    }

    public String atualizar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return null;
        }
        if (!emailValido(email)) {
            addMensagemErro("Digite um email válido");
            return null; // ou return; dependendo se o método retorna String ou void
        }
        
        if (role == null) {
            addMensagemErro("A role é obrigatória");
            return null;
        }
        if (empresasIds == null || empresasIds.isEmpty()) {
            addMensagemErro("O usuário deve pertencer a pelo menos uma empresa");
            return null;
        }
        try {
            UsuarioRequestDTO dto = new UsuarioRequestDTO(nome, email, senha, role, empresasIds);
            usuarioService.atualizar(id, dto);
            return "/pages/admin/usuarios.xhtml?faces-redirect=true";
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
            return null;
        }
    }

    public RoleEnum[] getRoles() { return RoleEnum.values(); }

    private void addMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, mensagem, null));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; }
    public List<Long> getEmpresasIds() { return empresasIds; }
    public void setEmpresasIds(List<Long> empresasIds) { this.empresasIds = empresasIds; }
    public List<EmpresaResponseDTO> getEmpresasDisponiveis() { return empresasDisponiveis; }
}