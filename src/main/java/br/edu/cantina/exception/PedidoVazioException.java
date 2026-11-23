package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class PedidoVazioException extends CantinaException {

    public PedidoVazioException(Long numeroPedido) {
        super(HttpStatus.UNPROCESSABLE_CONTENT,
                String.format("O pedido nº %d não tem itens. Adicione ao menos um produto antes de finalizar.", numeroPedido));
    }
}
