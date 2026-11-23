package br.edu.cantina.pagamento;

import java.math.BigDecimal;

public record ResultadoPagamento(boolean aprovado, BigDecimal troco, String mensagem) {

    public static ResultadoPagamento aprovado(String mensagem) {
        return new ResultadoPagamento(true, BigDecimal.ZERO, mensagem);
    }
}
