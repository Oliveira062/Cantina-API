package br.edu.cantina.exception;

import br.edu.cantina.util.Dinheiro;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class ValorRecebidoInsuficienteException extends CantinaException {

    public ValorRecebidoInsuficienteException(BigDecimal total, BigDecimal recebido) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, montarMensagem(total, recebido == null ? BigDecimal.ZERO : recebido));
    }

    private static String montarMensagem(BigDecimal total, BigDecimal recebido) {
        return String.format("Valor recebido (%s) é menor que o total do pedido (%s). Faltam %s.",
                Dinheiro.formatar(recebido), Dinheiro.formatar(total), Dinheiro.formatar(total.subtract(recebido)));
    }
}
