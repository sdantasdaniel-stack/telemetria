package com.daniel.empresas.dto.request;

import java.util.List;

import com.daniel.empresas.model.RoleEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

//O que o cliente manda para cadastrar um usuário

//Request — campos que o cliente manda. Nunca tem id porque quem gera o ID é o banco. Nunca tem campos que o cliente não deve
//controlar.

// Os campos dos RequestDTOs precisam de anotações de validação:

//@NotBlank — para campos de texto que não podem ser vazios
//@Email — para campos de email

//@NotNull — para campos que não podem ser nulos (OU @notblank para STRINGS) A principal diferença é que @NotNull garante apenas
//que o valor não seja null, permitindo strings vazias ("") ou espaços em branco (" "), enquanto @NotBlank valida se o valor não
//é null e se contém pelo menos um caractere, removendo espaços em branco (trim) antes da verificação. @NotBlank é ideal para 
//campos String obrigatórios.


//O campo ativo não entra — quando um usuário é cadastrado ele já nasce ativo. O sistema define isso, não o cliente.

public record UsuarioRequestDTO(

        @NotBlank(message = "O nome não pode estar em branco")
        String nome,

        @NotBlank(message = "O e-mail não pode estar em branco")
        @Email(message = "O e-mail deve ser válido")
        String email,

        @NotBlank(message = "A senha não pode estar em branco")
        String senha,

        // role não pode ser nula — deve ser ADMIN ou USER
        @NotNull(message = "A role é obrigatória")
        RoleEnum role,

        // lista de IDs das empresas que o usuário vai pertencer
        // não pode ser vazia — todo usuário precisa pertencer a pelo menos uma empresa
        @NotEmpty(message = "O usuário deve pertencer a pelo menos uma empresa")
        List<Long> empresasIds

) {}
