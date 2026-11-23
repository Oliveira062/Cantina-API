package br.edu.cantina.pagamento;

import br.edu.cantina.exception.ValorRecebidoInsuficienteException;
import br.edu.cantina.util.Dinheiro;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PagamentoDinheiro implements EstrategiaPagamento {

    @Override
    public ResultadoPagamento processar(BigDecimal total, BigDecimal valorRecebido) {
        if (valorRecebido == null || valorRecebido.compareTo(total) < 0) {
            throw new ValorRecebidoInsuficienteException(total, valorRecebido);
        }
        BigDecimal troco = valorRecebido.subtract(total);
        return new ResultadoPagamento(true, troco,
                "Pagamento em dinheiro aprovado. Troco: " + Dinheiro.formatar(troco) + ".");
    }

    @Override
    public TipoPagamento getTipo() {
        return TipoPagamento.DINHEIRO;
    }
}
