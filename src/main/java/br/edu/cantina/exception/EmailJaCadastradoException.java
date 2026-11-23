package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class EmailJaCadastradoException extends CantinaException {

    public EmailJaCadastradoException() {
        super(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
    }
}
