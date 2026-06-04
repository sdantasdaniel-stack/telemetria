package com.daniel.empresas.beans.admin;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.service.EmpresaService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

//Para que a sessão fosse 100% stateless, nao poderia usar JSF primefaces, pois ele  guarda o estado da view na sessão.

// gerenciado pelo Spring — acessível nas páginas xhtml pelo nome "adminEmpresaBean"
@Component("adminEmpresaBean")

//O bean vive enquanto você estiver na mesma página web
@ViewScoped
public class AdminEmpresaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //É um @Component com @RequestScope — gerenciado pelo JSF junto com o Spring
    //O JSF instancia o bean primeiro, depois o Spring injeta as dependências
    //Por causa dessa "dupla gestão", o @RequiredArgsConstructor pode não funcionar corretamente
    //O @Autowired por campo é mais seguro nesse contexto
    @Autowired
    private EmpresaService empresaService;

    // injetado apenas para o teste da Hipótese A — logar o hash e comparar com o do Notificador e do Controller REST
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // lista de empresas exibida na tabela da página
    private List<EmpresaResponseDTO> empresas;

    // empresa selecionada na tabela para edição ou exclusão
    private EmpresaResponseDTO empresaSelecionada;

    // campos do formulário de cadastro e edição
    private String nome;
    private String cnpj;
    private String email;

    // carrega a lista de empresas do banco — chamado ao entrar na página
    // f:event type="preRenderView" no xhtml chama esse método antes de renderizar
    public void carregar() {
        empresas = empresaService.listarTodas();
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

    // cadastra uma nova empresa com os dados do formulário
    public void cadastrar() {
        // validação manual — substitui o required="true" do xhtml para tentar resolver problema das constraints dispararem antes
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
            // loga o hash do template injetado no Bean JSF — comparar com o hash do Notificador e do Controller REST
            System.out.println(">>> JSF Bean template hash: " + System.identityHashCode(messagingTemplate));
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