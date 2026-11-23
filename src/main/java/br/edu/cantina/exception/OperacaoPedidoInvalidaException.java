package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class OperacaoPedidoInvalidaException extends CantinaException {

    public OperacaoPedidoInvalidaException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
