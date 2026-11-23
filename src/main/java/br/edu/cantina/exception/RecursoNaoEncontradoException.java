package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends CantinaException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
