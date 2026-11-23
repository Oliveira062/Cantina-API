package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class LoginBloqueadoException extends CantinaException {

    public LoginBloqueadoException(long minutosRestantes) {
        super(HttpStatus.TOO_MANY_REQUESTS, String.format(
                "Muitas tentativas de login sem sucesso. Tente novamente em %d minuto(s).", minutosRestantes));
    }
}
