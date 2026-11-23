package br.edu.cantina.pagamento;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PagamentoFactory {

    private final Map<TipoPagamento, EstrategiaPagamento> estrategias = new EnumMap<>(TipoPagamento.class);

    public PagamentoFactory(List<EstrategiaPagamento> disponiveis) {
        for (EstrategiaPagamento estrategia : disponiveis) {
            estrategias.put(estrategia.getTipo(), estrategia);
        }
    }

    public EstrategiaPagamento criar(String tipo) {
        return criar(TipoPagamento.de(tipo));
    }

    public EstrategiaPagamento criar(TipoPagamento tipo) {
        EstrategiaPagamento estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new IllegalStateException("Nenhuma estratégia registrada para " + tipo);
        }
        return estrategia;
    }
}
