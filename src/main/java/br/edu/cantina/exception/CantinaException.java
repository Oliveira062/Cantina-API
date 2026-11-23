package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public abstract class CantinaException extends RuntimeException {

    private final HttpStatus status;

    protected CantinaException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
