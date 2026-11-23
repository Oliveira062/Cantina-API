package br.edu.cantina.pagamento;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PagamentoPix implements EstrategiaPagamento {

    @Override
    public ResultadoPagamento processar(BigDecimal total, BigDecimal valorRecebido) {
        return ResultadoPagamento.aprovado("Pagamento via PIX aprovado.");
    }

    @Override
    public TipoPagamento getTipo() {
        return TipoPagamento.PIX;
    }
}
