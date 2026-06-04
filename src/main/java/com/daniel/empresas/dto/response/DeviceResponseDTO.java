package com.daniel.empresas.dto.response;

import com.daniel.empresas.model.StatusEnum;

//Response — campos que o sistema devolve. Nunca tem senha. Só expõe o que o cliente precisa ver.

//O que o sistema devolve quando alguém busca um device

public record DeviceResponseDTO(
		Long id, String nome,
		String identificador,
		StatusEnum status,
		boolean ativo,
		Long empresaId,
		Double latitude,
		Double longitude) {

}
