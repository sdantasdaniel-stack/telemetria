package com.daniel.empresas.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.daniel.empresas.model.Usuario;

//marca a classe para o Spring gerenciá-la e saber que essa classe tem lógica de negócio.

@Service
public class JwtService {
	
	// Essa anotação serve para dizer onde o spring deve buscar essa chave no aplication properties
	// Chave do algotirmo que gera e valida os tokens
	
	@Value("${jwt.secret}")
	private String secret;
	
	// Tempo que dura o token
	
	@Value("${jwt.expiration}")
	private long expiration;
	
	// método que Recebe um objeto Usuario e devolve uma String JWT.
	public String gerarToken(Usuario usuario){								
		
	    try {
            // Construtor do algoritmo
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer("sistema")
                    .withSubject(usuario.getEmail())
                    /*
                     * Adiciona a role do usuario como claim no token JWT.
                     * Isso permite que o JwtFilter extraia a role diretamente
                     * do token sem precisar consultar o banco de dados,
                     * eliminando a necessidade de sessao HTTP para controle de roles.
                     */
                    .withClaim("role", usuario.getRole().name())
                    .withExpiresAt(Instant.now().plusMillis(expiration))
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while authenticating");
        }
	    
	 	
	}
	// método que valida o token
	public String validarToken(String token) {

	        try {
	            Algorithm algorithm = Algorithm.HMAC256(secret);
	            return JWT.require(algorithm)
	                    .withIssuer("sistema")
	                    .build()
	                    .verify(token)
	                    .getSubject();

	        } catch (JWTVerificationException exception) {
	            return null;
	        }
	    }
	 
	 // serve para ver quanto tempo falta para o token expirar
	 public long getExpiresIn(String token) {
	        try {
	            Algorithm algorithm = Algorithm.HMAC256(secret);
	            Instant expiresAt = JWT.require(algorithm)
	                    .withIssuer("sistema")
	                    .build()
	                    .verify(token)
	                    .getExpiresAtAsInstant();
	            
	            // ChronoUnit.SECONDS - É um enum do Java que representa uma unidade de tempo — no caso, segundos
	            // .between() - É um método do ChronoUnit que  subtrai o primeiro do segundo termo: hora que expira - hora atual
	            
	            // ou seja, se o try der certo, retorna em quanto tempo o token expira

	            return ChronoUnit.SECONDS.between(Instant.now(), expiresAt);
	            	           
	        // Se o TRY nao der certo, uma JWTVerificationException vai ser gerada e o catch vai pegar ela e retornar 0 segundos, ou seja, que 
	        // o token expirou.
	            
	        } catch (JWTVerificationException exception) {
	            return 0;
	        }
	    }

	        /*
	         * Extrai a role do usuario diretamente do token JWT.
	         * Elimina a necessidade de consultar o banco ou a sessao HTTP
	         * para saber qual role o usuario possui.
	         * Retorna null se o token for invalido.
	         */
	  // Esse método é chamado pelos métodos isAdmin() e isUser() do LoginBean
	  public String getRoleFromToken(String token) {
	            try {
	                Algorithm algorithm = Algorithm.HMAC256(secret);
	                return JWT.require(algorithm)
	                        .withIssuer("sistema")
	                        .build()
	                        .verify(token)
	                        .getClaim("role")
	                        .asString();
	            
	            } catch (JWTVerificationException exception) {
	                return null;
	            }
	  }
}
