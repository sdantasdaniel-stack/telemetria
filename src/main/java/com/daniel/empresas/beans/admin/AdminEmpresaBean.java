package com.daniel.empresas.beans.admin;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.service.EmpresaService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

/**
 * Bean JSF do painel administrativo para gerenciamento de empresas.
 * Responsável por listar, cadastrar, desativar, reativar e deletar empresas.
 *
 * Escopo de view: o estado é preservado enquanto o usuário permanecer na mesma página.
 */
@Component("adminEmpresaBean")
@ViewScoped
public class AdminEmpresaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private EmpresaService empresaService;

    // lista de empresas exibida na tabela da página
    private List<EmpresaResponseDTO> empresas;

    // empresa selecionada na tabela para edição ou exclusão
    private EmpresaResponseDTO empresaSelecionada;

    // campos do formulário de cadastro e edição
    private String nome;
    private String cnpj;
    private String email;

    /**
     * Carrega a lista de empresas do banco de dados.
     * Chamado via f:event type="preRenderView" no XHTML antes de renderizar a página.
     */
    public void carregar() {
        empresas = empresaService.listarTodas();
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
     * Cadastra uma nova empresa com os dados do formulário.
     * A validação é feita no Bean (e não via required="true" no XHTML) para evitar
     * que as constraints do Bean Validation disparem antes do fluxo esperado.
     */
    public void cadastrar() {
        if (nome == null || nome.isBlank()) {
            addMensagemErro("O nome é obrigatório");
            return;
        }
        if (cnpj == null || cnpj.isBlank()) {
            addMensagemErro("O CNPJ é obrigatório");
            return;
        }
        if (!emailValido(email)) {
            addMensagemErro("Digite um email válido");
            return;
        }
        try {
            EmpresaRequestDTO dto = new EmpresaRequestDTO(nome, cnpj, email);
            empresaService.cadastrar(dto);
            carregar();
            limparCampos();
            addMensagemSucesso("Empresa cadastrada com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // desativa a empresa selecionada
    public void desativar(EmpresaResponseDTO empresa) {
        try {
            empresaService.desativar(empresa.id());
            carregar();
            addMensagemSucesso("Empresa desativada com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // limpa os campos do formulário após operação
    private void limparCampos() {
        nome = null;
        cnpj = null;
        email = null;
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

    // deleta a empresa permanentemente
    public void deletar(EmpresaResponseDTO empresa) {
        try {
            empresaService.deletar(empresa.id());
            carregar();
            addMensagemSucesso("Empresa deletada permanentemente");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // reativa a empresa selecionada
    public void reativar(EmpresaResponseDTO empresa) {
        try {
            empresaService.reativar(empresa.id());
            carregar();
            addMensagemSucesso("Empresa reativada com sucesso");
        } catch (Exception e) {
            addMensagemErro(e.getMessage());
        }
    }

    // getters e setters
    public List<EmpresaResponseDTO> getEmpresas() { return empresas; }
    public EmpresaResponseDTO getEmpresaSelecionada() { return empresaSelecionada; }
    public void setEmpresaSelecionada(EmpresaResponseDTO empresaSelecionada) { this.empresaSelecionada = empresaSelecionada; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}