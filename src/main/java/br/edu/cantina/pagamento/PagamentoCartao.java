package br.edu.cantina.pagamento;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PagamentoCartao implements EstrategiaPagamento {

    @Override
    public ResultadoPagamento processar(BigDecimal total, BigDecimal valorRecebido) {
        return ResultadoPagamento.aprovado("Pagamento no cartão aprovado.");
    }

    @Override
    public TipoPagamento getTipo() {
        return TipoPagamento.CARTAO;
    }
}
