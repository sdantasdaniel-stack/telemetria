package com.daniel.empresas.dto.response;


import java.util.List;

//String tipo é para poder dizer que é do tipo Bearer

// A lista é do tipo EmpresaResponseDTO e não Empresa por segurança e boas práticas — o EmpresaResponseDTO expõe só os campos necessários
// (id, nome, cnpj, email, ativo). Se usasse a entidade Empresa diretamente, você correria o risco de expor dados internos ou causar problemas 
// de serialização com o JPA, como carregar relacionamentos desnecessários.
public record LoginResponseDTO(String nome, String token, String tipo, List<EmpresaResponseDTO> empresas) {

}
