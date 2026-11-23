package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends CantinaException {

    public CredenciaisInvalidasException() {
        super(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }
}
