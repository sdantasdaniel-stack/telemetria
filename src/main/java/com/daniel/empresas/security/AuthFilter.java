package com.daniel.empresas.security;

import java.io.IOException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.daniel.empresas.model.RoleEnum;
import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.UsuarioRepository;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// AuthFilter — cobre as páginas .xhtml do PrimeFaces. O problema é que o PrimeFaces é um framework JSF — ele roda fora do 
// contexto do Spring Security, então o JwtFilter sozinho não protege essas páginas. Por isso precisou de um Servlet Filter 
// separado que intercepta as URLs /pages/* e faz o redirecionamento para o login manualmente.

// @WebFilter registra essa classe como um Servlet Filter

// SERVLETFILTER é um componente que intercepta requisições HTTP antes que elas cheguem ao controller,
// e também pode interceptar a resposta antes de voltar ao cliente. Funciona como uma barreira que toda requisição precisa passar

// urlPatterns define quais URLs esse filter vai interceptar

// /pages/* significa qualquer página dentro da pasta pages — todas as páginas protegidas



@WebFilter(urlPatterns = "/pages/*")
public class AuthFilter implements Filter {

    // doFilter é executado a cada requisição interceptada
    // ServletRequest e ServletResponse são as versões genéricas — fazemos cast para Http
    // para ter acesso a cookies e redirect
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

     // ServletContext é a interface do Jakarta que representa o contexto da aplicação web inteira.
     // Ela conhece tudo que está deployado — arquivos, configurações e outros contextos como o do Spring.
     // getServletContext() é um método do HttpServletRequest que retorna esse contexto.
     ServletContext servletContext = httpRequest.getServletContext();

     // WebApplicationContext é o contexto do Spring — é onde ficam guardados todos os beans gerenciados por ele.
     // WebApplicationContextUtils é uma classe utilitária do Spring que serve como ponte entre 
     // o mundo Servlet (Jakarta) e o mundo Spring.
     // getWebApplicationContext() recebe o ServletContext e devolve o contexto do Spring que está
     // registrado dentro dele.
     WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

     // getBean() é o método do Spring que busca um bean pelo tipo dentro do contexto.
     // Aqui está pedindo a instância do JwtService que o Spring gerencia.
     // Isso é necessário porque o AuthFilter é um Servlet Filter puro — não é gerenciado pelo Spring,
     // então não pode usar @Autowired. A solução é buscar o bean manualmente pelo contexto.
     JwtService jwtService = springContext.getBean(JwtService.class);

     // Mesma lógica — busca manualmente a instância do UsuarioRepository que o Spring gerencia,
     // já que @Autowired não funciona aqui.
     UsuarioRepository usuarioRepository = springContext.getBean(UsuarioRepository.class);

        // busca o cookie "jwt-token" nos cookies da requisição
        // o browser envia automaticamente todos os cookies do domínio em cada requisição
        // não é necessário passar o token manualmente como no cabeçalho Authorization da API REST
     
     	// String token = null; serve para inicializar a variável antes do bloco de busca. Em Java você não pode usar uma 
     	// variável local sem ela ter sido inicializada. Ela informa que o valor de token começa como nulo.
        String token = null;
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
            	
            	// no login eu defini o cookie como: Cookie jwtCookie = new Cookie("jwt-token", token);
                if ("jwt-token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // valida o token JWT — retorna o email do usuário se válido, null se inválido ou expirado
        // valida chamando o método validarToken do jwtservice
        String emailValidado = null;
        if (token != null) {
            emailValidado = jwtService.validarToken(token);
        }

        // token ausente ou inválido — redireciona para o login
        // getContextPath() retorna o caminho raiz da aplicação
        if (emailValidado == null) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/login.xhtml");
            return;
        }

        // token válido — busca o usuário no banco pelo email extraído do token
        // o mesmo objeto usuario é reutilizado para verificar a role e renovar o token
        // evitando consultas duplicadas ao banco
        Usuario usuario = usuarioRepository.findByEmail(emailValidado).orElse(null);

        // URL que o usuário está tentando acessar
        String requestedUrl = httpRequest.getRequestURI();
        
        // garante que usuario nulo seja redirecionado e nao consiga entrar através de link direto
        if (usuario == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
            return;
        }
        // para impedir que usuario que tenha sido mudado para inativo enquanto estava online possa continuar mexendo 
        // (só vai deslogar ele no primeiro clique que ele der depois que estiver como inativo) 
        if (!usuario.isAtivo()) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
            return;
        }

        // controle de acesso por role — impede que USER acesse páginas de ADMIN mesmo digitando a URL diretamente,
        // redirecionando o usuário para a página de sua ROLE
        if (usuario.getRole() == RoleEnum.USER && requestedUrl.contains("/pages/admin/")) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/pages/user/devices.xhtml");
            return;
        }

        // controle de acesso por role — impede que ADMIN acesse páginas de USER mesmo digitando a URL diretamente,
        // redirecionando o usuário para a página de sua ROLE
        if (usuario.getRole() == RoleEnum.ADMIN && requestedUrl.contains("/pages/user/")) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/pages/admin/dashboard.xhtml");
            return;
        }

        // renovação automática do token JWT
        // verifica quantos segundos restam para o token expirar
        // se restar menos de 120 segundos (2 minutos), gera um novo token
        // e substitui o cookie — o usuário não percebe e continua logado
        // se o usuário ficar inativo por 5 minutos o token expira e ele é redirecionado para o login
        long segundosRestantes = jwtService.getExpiresIn(token);
        if (segundosRestantes < 120 && usuario != null) {
            String novoToken = jwtService.gerarToken(usuario);
            Cookie novoCookie = new Cookie("jwt-token", novoToken);
            // HttpOnly impede que JavaScript acesse o cookie — proteção contra XSS
            novoCookie.setHttpOnly(true);
            
            // XSS (Cross-Site Scripting) é um tipo de ataque onde um hacker consegue injetar código JavaScript malicioso na sua 
            // página. Se conseguir, o JavaScript dele poderia fazer isso:
            // Quando você marca o cookie como HttpOnly, O browser passa a bloquear qualquer acesso ao cookie via JavaScript.
            // com isso, se eu tiver duas aplicações no mesmo Tomcat, o cookie da sua app NÃO vai vazar pra outra
            novoCookie.setPath(httpRequest.getContextPath() + "/");
            // maxAge define por quanto tempo o cookie fica no browser — 24 horas
            // o token JWT expira em 5 minutos mas o cookie dura mais — o AuthFilter controla a validade real
            novoCookie.setMaxAge(60 * 60 * 24);
            httpResponse.addCookie(novoCookie);
        }

        // token válido e role correta — deixa a requisição continuar
        // para o FacesServlet processar a página xhtml
        // Serve para passar a requisição para o próximo elemento da cadeia.
        chain.doFilter(request, response);
    }
}
