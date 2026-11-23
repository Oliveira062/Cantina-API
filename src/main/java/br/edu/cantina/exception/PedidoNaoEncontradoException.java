package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class PedidoNaoEncontradoException extends CantinaException {

    public PedidoNaoEncontradoException(Long numero) {
        super(HttpStatus.NOT_FOUND, String.format("Pedido nº %d não encontrado. Confira o número informado.", numero));
    }
}
