package com.daniel.empresas.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daniel.empresas.dto.request.UsuarioRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.dto.response.UsuarioComEmpresasIdDTO;
import com.daniel.empresas.dto.response.UsuarioResponseDTO;
import com.daniel.empresas.model.RoleEnum;
import com.daniel.empresas.service.EmpresaService;
import com.daniel.empresas.service.UsuarioService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

//Para que a sessão fosse 100% stateless, nao poderia usar JSF primefaces, pois ele  guarda o estado da view na sessão.


@Component("adminUsuarioBean")
@ViewScoped
// O bean vive enquanto você estiver na mesma página web
public class AdminUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    
    //É um @Component com @RequestScope — gerenciado pelo JSF junto com o Spring
    //O JSF instancia o bean primeiro, depois o Spring injeta as dependências
    //Por causa dessa "dupla gestão", o @RequiredArgsConstructor pode não funcionar corretamente
    //O @Autowired por campo é mais seguro nesse contexto
    @Autowired
    private UsuarioService usuarioService;

    // necessário para carregar a lista de empresas disponíveis para seleção
    @Autowired
    private EmpresaService empresaService;

    private List<UsuarioComEmpresasIdDTO> usuarios;
    private UsuarioResponseDTO usuarioSelecionado;

    // lista de todas as empresas disponíveis — usada no componente de seleção
    private List<EmpresaResponseDTO> empresasDisponiveis;

    // IDs das empresas selecionadas para o usuário
    private List<Long> empresasIds = new ArrayList<>();

    private String nome;
    private String email;
    private String senha;
    private RoleEnum role;

    // carrega a lista de usuários e empresas disponíveis
    public void carregar() {
    	
        usuarios = usuarioService.listarTodos();
        // carrega todas as empresas ativas para o componente de seleção
        empresasDisponiveis = empresaService.listarTodas();
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

    // cadastra um novo usuário com os dados do formulário
    public void cadastrar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return;
        }
        if (!emailValido(email)) {
            addMensagemErro("Digite um email válido");
            return;
        
        }
        if (senha == null || senha.isBlank()) {
            addMensagemErro("A senha é obrigatória");
            return;
        }
        if (role == null) {
            addMensagemErro("A role é obrigatória");
            return;
        }
        if (empresasIds == null || empresasIds.isEmpty()) {
            addMensagemErro("O usuário deve pertencer a pelo menos uma empresa");
            return;
        }
        try {
            UsuarioRequestDTO dto = new UsuarioRequestDTO(nome, email, senha, role, empresasIds);
            usuarioService.cadastrar(dto);
            carregar();
            limparCampos();
            addMensagemSucesso("Usuário cadastrado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // desativa o usuário selecionado
    public void desativar(UsuarioResponseDTO usuario) {
        try {
            usuarioService.desativar(usuario.id());
            carregar();
            addMensagemSucesso("Usuário desativado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // reativa o usuário selecionado
    public void reativar(UsuarioResponseDTO usuario) {
        try {
            usuarioService.reativar(usuario.id());
            carregar();
            addMensagemSucesso("Usuário reativado com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // deleta o usuário permanentemente
    public void deletar(UsuarioResponseDTO usuario) {
        try {
            usuarioService.deletar(usuario.id());
            carregar();
            addMensagemSucesso("Usuário deletado permanentemente");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // retorna os valores do enum RoleEnum para o dropdown de seleção de role
    public RoleEnum[] getRoles() {
        return RoleEnum.values();
    }

    private void limparCampos() {
        nome = null;
        email = null;
        senha = null;
        role = null;
        empresasIds = new ArrayList<>();
        
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

    // getters e setters
    public List<UsuarioComEmpresasIdDTO> getUsuarios() { return usuarios; }
    public UsuarioResponseDTO getUsuarioSelecionado() { return usuarioSelecionado; }
    public void setUsuarioSelecionado(UsuarioResponseDTO u) { this.usuarioSelecionado = u; }
    public List<EmpresaResponseDTO> getEmpresasDisponiveis() { return empresasDisponiveis; }
    public List<Long> getEmpresasIds() { return empresasIds; }
    public void setEmpresasIds(List<Long> empresasIds) { this.empresasIds = empresasIds; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; }
}