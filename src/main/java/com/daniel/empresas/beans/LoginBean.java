package com.daniel.empresas.beans;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.daniel.empresas.model.RoleEnum;
import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.UsuarioRepository;
import com.daniel.empresas.security.JwtService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Para que a sessão fosse 100% stateless, nao poderia usar JSF primefaces, pois ele  guarda o estado da view na sessão.

// @Component("loginBean") — torna o bean acessível nas páginas xhtml pelo nome "loginBean"
// ex: #{loginBean.email} acessa o campo email desta classe
// gerenciado pelo Spring, não pelo CDI, por consistência com o restante do projeto
@Component("loginBean")

// @RequestScope — o Bean vive apenas durante uma requisição
// adequado para o login porque após redirecionar o Bean não precisa mais existir

//Se quisesse que o CDI gerenciasse ao invés do spring, usaria @RequestScoped
@RequestScope
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    
    //É um @Component com @RequestScope — gerenciado pelo JSF junto com o Spring
    //O JSF instancia o bean primeiro, depois o Spring injeta as dependências
    //Por causa dessa "dupla gestão", o @RequiredArgsConstructor pode não funcionar corretamente
    //O @Autowired por campo é mais seguro nesse contexto
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // injeta o JwtService para gerar o token JWT após autenticação bem sucedida
    @Autowired
    private JwtService jwtService;

    // campos vinculados ao formulário de login pelas ELs do xhtml
    // #{loginBean.email} e #{loginBean.senha}
    private String email;
    private String senha;

    // método chamado pelo botão de login do xhtml — #{loginBean.login()}
    // retorna String porque o JSF usa o retorno para navegar para outra página
    public String login() {

        // busca o usuário no banco pelo email digitado
        // retorna null se não encontrar — sem lançar exceção
    	
    	Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
    	
    	if (usuario == null) {
    	    
    	    addMensagemErro("Credenciais inválidas");
    	    return null;
    	}
    	
    	// O matches pega a senha em texto puro que o usuário acabou de digitar, aplica o mesmo algoritmo de criptografia usado para
    	// criptografar a senha original e compara os hashs. Como tem o "!"(operador de negação), se nao baterem, mostra a mensagem
    	
    	if (!passwordEncoder.matches(senha, usuario.getSenha())) {
    	    
    	    addMensagemErro("Credenciais inválidas");
    	    return null;
    	}
    	
        
        // para que o usuario que está com ativo = false nao possa entrar
        if (!usuario.isAtivo()) {
            addMensagemErro("Usuário inativo. Entre em contato com o administrador.");
            return null;
        }

        // gera o token JWT usando o mesmo JwtService da API REST
        String token = jwtService.gerarToken(usuario);

        // salva o token JWT em um cookie HttpOnly
        // HttpOnly = true — o JavaScript do browser não consegue ler o cookie
        // protege contra ataques XSS
        // o browser envia o cookie automaticamente em toda requisição
        // eliminando a necessidade de passar o token manualmente
        Cookie jwtCookie = new Cookie("jwt-token", token);
        jwtCookie.setHttpOnly(true);
        // com isso, se eu tiver duas aplicações no mesmo Tomcat, o cookie da sua app NÃO vai vazar pra outra
        jwtCookie.setPath(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getContextPath() + "/");
        // maxAge 24 horas — o AuthFilter controla a validade real pelo token JWT
        jwtCookie.setMaxAge(60 * 60 * 24);

        // adiciona o cookie na resposta HTTP
        HttpServletResponse httpResponse = (HttpServletResponse)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getResponse();
        httpResponse.addCookie(jwtCookie);

        // redireciona para a página correta conforme a role do usuário
        // faces-redirect=true instrui o JSF a fazer um redirect HTTP em vez de forward
        // isso atualiza a URL no browser e evita resubmissão do formulário
        if (usuario.getRole() == RoleEnum.ADMIN) {
            return "/pages/admin/dashboard.xhtml?faces-redirect=true";
        } else {
            return "/pages/user/devices.xhtml?faces-redirect=true";
        }
    }

    // método chamado pelo botão de logout
    // remove o cookie JWT — não invalida sessão pois a interface é stateless***
    public String logout() {
        // cria um cookie vazio com maxAge 0 — instrui o browser a deletar o cookie
        Cookie jwtCookie = new Cookie("jwt-token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getContextPath() + "/");
        // maxAge 0 instrui o browser a deletar o cookie imediatamente
        jwtCookie.setMaxAge(0);

        // tive que trocar de RequestContextHolder para facescontext, pois o do spring nao estava pegando, provavelm. pelo JSF 
        // instanciar o bean primeiro 
        HttpServletResponse httpResponse = (HttpServletResponse)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getResponse();
        httpResponse.addCookie(jwtCookie);

        // redireciona para o login após o logout
        return "/login.xhtml?faces-redirect=true";
    }

    // método usado no atributo rendered dos componentes xhtml
    // ex: rendered="#{loginBean.admin}"
    // o JSF chama isAdmin() automaticamente pela convenção JavaBean
    // permite mostrar ou esconder componentes conforme a role do usuário
    public boolean isAdmin() {
        String role = getRoleFromCookie();
        return RoleEnum.ADMIN.name().equals(role);
    }

    // mesmo conceito do isAdmin() mas para a role USER
    public boolean isUser() {
        String role = getRoleFromCookie();
        return RoleEnum.USER.name().equals(role);
    }

    // método privado auxiliar que lê o token JWT do cookie da requisição
    // e extrai a role usando o JwtService
    // retorna null se o cookie não existir ou o token for inválido
    private String getRoleFromCookie() {
        HttpServletRequest httpRequest = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt-token".equals(cookie.getName())) {
                    return jwtService.getRoleFromToken(cookie.getValue());
                }
            }
        }
        return null;
    }

    // método auxiliar para adicionar mensagem de erro na página
    // o componente p:messages do xhtml exibe essa mensagem automaticamente
    private void addMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, mensagem, null));
    }

    // retorna o nome do usuário logado extraindo o email do token JWT
    // e buscando o nome no banco — usado nas páginas para exibir "Bem vindo, [nome]"
    public String getNomeUsuario() {
        HttpServletRequest httpRequest = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt-token".equals(cookie.getName())) {
                    String emailDoToken = jwtService.validarToken(cookie.getValue());
                    if (emailDoToken != null) {
                        return usuarioRepository.findByEmail(emailDoToken)
                                .map(u -> u.getNome())
                                .orElse("Usuário");
                    }
                }
            }
        }
        return "Usuário";
    }
    
    // verifica se o usuário já está logado ao entrar na página de login
    // se tiver cookie JWT válido, redireciona para a página correta sem mostrar o formulário
    public void redirecionarSeLogado() {
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
                     	if (usuario != null && usuario.isAtivo()) {
                     		try {
                     			String destino = usuario.getRole() == RoleEnum.ADMIN
                                     ? "/pages/admin/dashboard.xhtml"
                                     : "/pages/user/devices.xhtml";
                             FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .redirect(FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getRequestContextPath() + destino);
                         } catch (Exception e) {
                             // se o redirect falhar, deixa a página de login carregar normalmente
                         }
                     }
                 }
             }
         }
     }
 }
    
    
    

    // O EL - Expression Language do Jakarta EE resolve os getters e setters em tempo de execução usando reflexão — ele procura 
    //literalmente por métodos chamados getEmail() e setEmail() na classe. O problema é que o Lombok gera esses métodos em tempo 
    //de compilação, e dependendo da configuração do projeto o Jakarta EE não enxerga esses métodos gerados.
    // Para evitar esse problema usei getters e setters manualmente, garantindo que o EL vai encontrá-los sem depender do Lombok.
    // getters e setters — necessários para o PrimeFaces vincular os campos do formulário
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}