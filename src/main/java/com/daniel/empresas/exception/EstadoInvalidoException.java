package com.daniel.empresas.exception;


public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
