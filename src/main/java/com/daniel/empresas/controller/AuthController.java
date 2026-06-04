package com.daniel.empresas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniel.empresas.dto.request.LoginRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.dto.response.LoginResponseDTO;
import com.daniel.empresas.model.Usuario;

import com.daniel.empresas.security.JwtService;

import lombok.RequiredArgsConstructor;

// USADO APENAS PARA O LOGIN NA API REST

// Diz ao Spring que essa classe é um controller REST, ou seja, ela vai receber requisições HTTP e retornar dados (geralmente JSON).
@RestController

// Essa anotação mapeia URLs de requisições HTTP para classes ou métodos específicos do seu controlador
// Define o caminho base de todas as rotas desse controller. Então toda rota dentro dessa classe vai começar com /auth
@RequestMapping("/auth")

// Anotação do Lombok. Ela gera automaticamente um construtor com todos os atributos final da classe (serve para injeção)
@RequiredArgsConstructor

public class AuthController {


    private final JwtService jwtService;

    // método marcado com bean, dentro de uma classe marcada como configuration não precisa de import, pois ele vai para o conteiner do spring
    private final AuthenticationManager authenticationManager;

    // @PostMapping do Spring é usada para mapear requisições HTTP POST em métodos de controladores REST
    // responde requisições POST na rota /auth/login
    @PostMapping("/login")

    // botei a interrogação pq pode vir um ResponseDTO ou nada
    // @RequestBody → pega o JSON que veio na requisição e transforma no objeto LoginRequestDTO
    // LoginRequestDTO body → objeto que contém email e senha enviados pelo cliente
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {

        // UsernamePasswordAuthenticationToken é uma classe do Spring Security. Ela representa um par de credenciais — usuário e senha — 
    	//num formato que o Spring Security entende. 
        // O AuthenticationManager vai usar isso para chamar o UserDetailsServiceImpl e comparar a senha com BCrypt
        var usernamePassword = new UsernamePasswordAuthenticationToken(body.email(), body.senha());

        // AuthenticationManager (método declarado no SecurityConfig) processa a autenticação — chama loadUserByUsername e verifica a senha
        // Se as credenciais forem inválidas ele lança uma exceção automaticamente — não precisa tratar manualmente
        // authenticate é o único método da interface AuthenticationManager
        
        var auth = authenticationManager.authenticate(usernamePassword);
        
        // authenticate retorna um objeto do tipo Authentication, que é uma interface que tem o método getPrincipal. Esse método:
        // Recupera o objeto Usuario autenticado do resultado da autenticação
        Usuario usuario = (Usuario) auth.getPrincipal();

        // Gera o token JWT com os dados do usuário
        String token = jwtService.gerarToken(usuario);

        // Converte a lista de Empresa do usuário para lista de EmpresaResponseDTO
        // O cliente precisa dessa lista para escolher qual empresa operar após o login
        // filtra empresas conforme a role do usuário
        // USER vê apenas ativas — ADMIN vê todas
        var empresas = usuario.getEmpresas().stream()
        		.filter(empresa -> usuario.getRole().name().equals("ADMIN") || empresa.isAtivo())
        		.map(empresa -> new EmpresaResponseDTO(
        				empresa.getId(),
        				empresa.getNome(),
        				empresa.getCnpj(),
        				empresa.getEmail(),
        				empresa.isAtivo()))
        		.toList();

        // Retorna o LoginResponseDTO com token, tipo Bearer, nome do usuário e lista de empresas
        return ResponseEntity.ok(new LoginResponseDTO(
                usuario.getNome(),
                token,
                "Bearer",
                empresas));
    }
}
