package com.daniel.empresas.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.daniel.empresas.security.JwtFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // CORS adicionado para o Angular — antes não era necessário porque PrimeFaces e backend
            // rodavam no mesmo servidor (mesma origem). Com o Angular em localhost:4200 fazendo
            // chamadas para o Spring em localhost:8080, o browser bloqueia por política de same-origin.
            // O .cors() aqui instrui o Spring Security a processar os preflight OPTIONS antes de
            // qualquer outro filtro, incluindo o JwtFilter — sem isso o Angular nem chega a mandar
            // o token porque o browser rejeita antes.

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/*.xhtml").permitAll()
                .requestMatchers("/pages/**").permitAll()
                .requestMatchers("/jakarta.faces.resource/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                .requestMatchers("/index.xhtml").permitAll()
                .requestMatchers("/login.xhtml").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws").permitAll()
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":401,\"mensagem\":\"Não autenticado\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":403,\"mensagem\":\"Acesso negado\"}");
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // origens permitidas — Angular em dev e em produção (ajuste o domínio quando for para prod)
        // allowedOrigins — apenas localhost:4200 em desenvolvimento.
        // Quando for para produção, trocar pelo domínio real do Angular (ex: https://sistema.com.br).
        // Nunca usar "*" com allowCredentials(true) — o browser rejeita essa combinação.

        config.setAllowedOrigins(List.of(
            "http://localhost:4200"
        ));

        // allowedMethods — todos os verbos que a API usa.
        // OPTIONS é obrigatório aqui: é o método do preflight que o browser manda antes de
        // qualquer requisição cross-origin com header Authorization.

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // expõe o header Authorization para o Angular conseguir ler o token renovado
        
        config.setExposedHeaders(List.of("Authorization"));

        // necessário para o Angular enviar o header Authorization
        config.setAllowCredentials(true);

        // registra a configuração CORS para todas as rotas da API.
        // sem isso o CORS só valeria para rotas específicas e o Angular quebraria em endpoints
        // que não estivessem mapeados explicitamente.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

