package br.edu.cantina.pagamento;

import java.math.BigDecimal;

public interface EstrategiaPagamento {

    ResultadoPagamento processar(BigDecimal total, BigDecimal valorRecebido);

    TipoPagamento getTipo();
}
