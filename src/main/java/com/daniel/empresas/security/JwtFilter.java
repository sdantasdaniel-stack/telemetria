package com.daniel.empresas.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// JwtFilter — cobre as requisições da API REST. Ele intercepta o token do cabeçalho Authorization, valida, e registra o usuário
// no SecurityContextHolder para o Spring Security liberar ou bloquear o acesso.

// @Component registra o filtro no contexto do Spring para que ele possa ser injetado no SecurityConfig

// OncePerRequestFilter - garante que o filtro seja executado apenas uma vez por requisição

@Component

//gera automaticamente um construtor contendo apenas os atributos que exigem inicialização.

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // JwtService é injetado pelo pelo spring através do @RequiredArgsConstructor — usado para validar o token
    private final JwtService jwtService;

    // UsuarioRepository é injetado pelo spring através do @RequiredArgsConstructor — usado para buscar o usuário no banco
    private final UsuarioRepository usuarioRepository;

    // anotação usada para sobreescrever método da classe pai
    @Override
    
    // método onde você define a lógica de interceptação para cada requisição HTTP que chega à sua aplicação
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extrai o token do cabeçalho Authorization da requisição
        var token = recoverToken(request);

        // Se não encontrou token no cabeçalho, tenta buscar no cookie jwt-token
        // Isso cobre as requisições feitas pelos Beans do PrimeFaces que usam cookie em vez de cabeçalho
        if (token == null) {
            token = recoverTokenFromCookie(request);
        }

        // Se o token existir, valida e registra o usuário no contexto de segurança
        if (token != null) {

            // Valida o token e retorna o email do usuário — retorna null se o token for inválido ou expirado
            var email = jwtService.validarToken(token);

            if (email != null) {

                Usuario usuario = usuarioRepository.findByEmailComEmpresas(email)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                // verifica se o usuário está ativo — se não estiver, rejeita a requisição com 401
                // cobre o caso de admin desativar um usuário que já estava logado com token válido
                if (!usuario.isAtivo()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":401,\"mensagem\":\"Usuário inativo\"}");
                    return;
                }

                var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())
                );
                

                // UsernamePasswordAuthenticationToken é a classe do spring security que representa o usuário autenticado
                // primeiro argumento: o objeto do usuário (principal)
                // segundo argumento: credenciais — null porque o token já foi validado, não precisa de senha
                // terceiro argumento: as permissões do usuário
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, authorities);

                // Registra o usuário autenticado no contexto de segurança do Spring
                // A partir daqui o Spring Security sabe quem está fazendo a requisição
                // getContext pega o contexto: email, role... 
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // verifica se o token está próximo de expirar
                // se restar menos de 120 segundos gera um novo token automaticamente
                // e devolve no cabeçalho da resposta para o cliente atualizar
                long segundosRestantes = jwtService.getExpiresIn(token);
                if (segundosRestantes < 120) {
                	String novoToken = jwtService.gerarToken(usuario);
                	
                	// adiciona o novo token no cabeçalho da resposta
                	// o cliente da API REST deve ler esse cabeçalho e substituir o token antigo
                	response.setHeader("Authorization", "Bearer " + novoToken);
             }
            }
        }

        // Passa a requisição para o próximo filtro da cadeia — obrigatório, sem isso a requisição trava
        filterChain.doFilter(request, response);
    }

    // Extrai o token do cabeçalho Authorization
    // O cabeçalho vem no formato "Bearer eyJhbGci..." — remove o prefixo "Bearer " para ficar só o token
    private String recoverToken(HttpServletRequest request) {
    	
        var authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) return null;
        
        // Significa que vai substituir Bearer por um espaço vazio, ou seja, só vai sobrar o token no authHeader
        
        return authHeader.replace("Bearer ", "");
    }

    // Extrai o token do cookie jwt-token
    // Usado quando a requisição vem da interface PrimeFaces — que usa cookie em vez de cabeçalho Authorization
    // O browser envia automaticamente todos os cookies do domínio em cada requisição
    private String recoverTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        // verifica se existem cookies na requisição — pode ser null se não houver nenhum
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // procura especificamente o cookie jwt-token
                if ("jwt-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // retorna null se não encontrar o cookie — o filtro vai deixar a requisição passar sem autenticar
        return null;
    }
}