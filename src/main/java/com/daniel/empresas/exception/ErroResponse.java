package com.daniel.empresas.exception;

import java.time.LocalDateTime;

// LocalDateTime é o único que precisa importar porque é o único que nao é tipo nativo do java

// A função desse arquivo é só determinar o formato da resposta do erro que vai aparecer para o usuario

public record ErroResponse(int status, String mensagem, LocalDateTime momento) {

}
