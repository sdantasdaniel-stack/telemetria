package com.daniel.empresas.beans.admin;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.service.EmpresaService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Bean JSF do painel administrativo para edição de empresas existentes.
 * Carrega os dados de uma empresa pelo ID recebido via parâmetro de URL e
 * permite atualizar suas informações.
 *
 * Escopo de sessão mantido para preservar o estado entre requisições JSF
 * durante o fluxo de edição.
 */
@Component("adminEmpresaEditarBean")
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

    /**
     * Carrega os dados da empresa pelo ID recebido como parâmetro de URL.
     * Deve ser chamado via f:event type="preRenderView" no XHTML.
     */
    public void carregar() {
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

    /**
     * Valida formato mínimo de e-mail: exige pelo menos um caractere antes e depois do '@'.
     */
    private boolean emailValido(String email) {
        if (email == null || email.isBlank()) return false;
        int arroba = email.indexOf('@');
        return arroba > 0 && arroba < email.length() - 1;
    }

    /**
     * Valida e salva as alterações da empresa.
     * @return caminho de redirecionamento para a listagem em caso de sucesso, ou null se houver erro de validação.
     */
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