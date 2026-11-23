package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class PagamentoInvalidoException extends CantinaException {

    public PagamentoInvalidoException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}
