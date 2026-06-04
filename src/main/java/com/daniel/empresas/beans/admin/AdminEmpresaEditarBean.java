package com.daniel.empresas.beans.admin;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.context.annotation.SessionScope;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.service.EmpresaService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Component("adminEmpresaEditarBean")
//@SessionScope — o Bean vive durante toda a sessão do usuario

@SessionScope
public class AdminEmpresaEditarBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private EmpresaService empresaService;

    // ID recebido da URL via f:viewParam
    private Long id;
    private String nome;
    private String cnpj;
    private String email;
    

    // carrega os dados da empresa pelo ID recebido da URL
    public void carregar() {
        // lê o parâmetro id diretamente da URL da requisição
        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("id");
        
        
        
        if (idParam != null && !idParam.isBlank()) {
            this.id = Long.parseLong(idParam);
            var empresa = empresaService.buscarPorId(this.id);
            this.nome = empresa.nome();
            this.cnpj = empresa.cnpj();
            this.email = empresa.email();
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

    // valida e salva as alterações — redireciona para listagem se sucesso
    public String atualizar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return null;
        }
        if (cnpj == null || cnpj.isBlank()) {
            addMensagemErro("O CNPJ é obrigatório");
            return null;
        }
        if (cnpj.length() != 14) {
            addMensagemErro("O CNPJ deve ter 14 dígitos");
            return null;
        }
        if (!emailValido(email)) {
            addMensagemErro("Digite um email válido");
            return null; 
        
        }
        try {
            EmpresaRequestDTO dto = new EmpresaRequestDTO(nome, cnpj, email);
            empresaService.atualizar(id, dto);
            return "/pages/admin/empresas.xhtml?faces-redirect=true";
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
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}