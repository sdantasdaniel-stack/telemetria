package com.daniel.empresas.dto.response;



//EmpresaResponseDTO - O que o sistema devolve quando alguém busca uma empresa


//Response — campos que o sistema devolve. Nunca tem senha. Só expõe o que o cliente precisa ver.


public record EmpresaResponseDTO(Long id, String nome, String cnpj, String email, boolean ativo) {

}
