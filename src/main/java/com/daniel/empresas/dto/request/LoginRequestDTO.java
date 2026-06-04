package com.daniel.empresas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//O que o cliente manda para cadastrar uma empresa

//Request — campos que o cliente manda. Nunca tem id porque quem gera o ID é o banco. Nunca tem campos que o cliente não deve
//controlar.

// Os campos dos RequestDTOs precisam de anotações de validação:

//@NotBlank — para campos de texto que não podem ser vazios
//@Email — para campos de email
//@NotNull — para campos que não podem ser nulos (OU @notblank para STRINGS) A principal diferença é que @NotNull garante apenas
//que o valor não seja null, permitindo strings vazias ("") ou espaços em branco (" "), enquanto @NotBlank valida se o valor não
//é null e se contém pelo menos um caractere, removendo espaços em branco (trim) antes da verificação. @NotBlank é ideal para 
//campos String obrigatórios.


//O campo ativo não entra — quando uma empresa é cadastrada ela já nasce ativa. O sistema define isso, não o cliente.

public record LoginRequestDTO(
		
		@NotBlank(message = "O e-mail não pode estar em branco")
	    @Email(message = "O e-mail deve ser válido")
		String email,
		
		@NotBlank(message = "A senha não pode estar em branco")
		String senha
		
		) {

}
