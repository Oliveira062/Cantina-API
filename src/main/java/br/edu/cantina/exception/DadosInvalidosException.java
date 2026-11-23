package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class DadosInvalidosException extends CantinaException {

    public DadosInvalidosException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}
