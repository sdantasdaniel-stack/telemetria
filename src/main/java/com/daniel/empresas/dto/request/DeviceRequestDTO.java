package com.daniel.empresas.dto.request;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//O que o cliente manda para cadastrar uma empresa

//Request — campos que o cliente manda. Nunca tem id porque quem gera o ID é o banco. Nunca tem campos que o cliente não deve
//controlar.
// Os campos dos RequestDTOs precisam de anotações de validação: 

//@NotBlank — para campos de texto que não podem ser vazios

//@NotNull — para campos que não podem ser nulos (OU @notblank para STRINGS) A principal diferença é que @NotNull garante apenas
//que o valor não seja null, permitindo strings vazias ("") ou espaços em branco (" "), enquanto @NotBlank valida se o valor não
//é null e se contém pelo menos um caractere, removendo espaços em branco (trim) antes da verificação. @NotBlank é ideal para 
//campos String obrigatórios.


//O campo status não entra — quando um device é cadastrado ele já nasce offline. Osistema que define isso.

public record DeviceRequestDTO(
		@NotBlank(message = "O nome não pode estar em branco")
		String nome,
		
		@NotBlank(message = "O identificador não pode estar em branco")
		String identificador,
		
		@NotNull(message = "Digite um IDempresa válido")
		Long empresaId,
		
		@Nullable
		Double latitude,

		@Nullable  
		Double longitude
		
		
		
		) {

}
