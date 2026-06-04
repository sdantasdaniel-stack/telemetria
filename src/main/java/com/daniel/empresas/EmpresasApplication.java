package com.daniel.empresas;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

//A anotação @SpringBootApplication é a anotação principal do Spring Boot, usada para indicar a classe de inicialização de uma 
//aplicação. Ela é uma anotação de "conveniência" que combina três funcionalidades essenciais: 
//@Configuration, @EnableAutoConfiguration e @ComponentScan.

//@Configuration: Marca a classe como uma fonte de definições de beans de configuração para o contexto da aplicação.
//@EnableAutoConfiguration: Ativa a autoconfiguração do Spring Boot, que adivinha e configura automaticamente os beans necessários 
//com base nas dependências do seu classpath.
//@ComponentScan: Habilita a varredura automática de componentes, permitindo que o Spring encontre e registre automaticamente 
//outras classes anotadas (como @Component, @Service, @Repository, @Controller) no pacote atual e subpacotes.

// instrui o Spring Boot a escanear e registrar componentes Servlet anotados com @WebFilter, @WebServlet e @WebListener. Sem ele, 
// o @WebFilter do AuthFilter é simplesmente ignorado na inicialização.
@ServletComponentScan
@SpringBootApplication
public class EmpresasApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmpresasApplication.class, args);
	}

}
