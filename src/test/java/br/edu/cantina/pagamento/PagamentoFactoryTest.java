package br.edu.cantina.pagamento;

import br.edu.cantina.exception.PagamentoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagamentoFactoryTest {

    private final PagamentoFactory factory =
            new PagamentoFactory(List.of(new PagamentoPix(), new PagamentoCartao(), new PagamentoDinheiro()));

    @Test
    @DisplayName("CT09 - forma de pagamento inexistente é rejeitada")
    void ct09PagamentoInexistente() {
        assertThrows(PagamentoInvalidoException.class, () -> factory.criar("BOLETO"));
        assertThrows(PagamentoInvalidoException.class, () -> factory.criar(" "));
    }

    @Test
    @DisplayName("Aceita o tipo com acento e em letras minúsculas")
    void aceitaVariacoesDeEscrita() {
        assertInstanceOf(PagamentoCartao.class, factory.criar("cartão"));
        assertInstanceOf(PagamentoPix.class, factory.criar("pix"));
        assertInstanceOf(PagamentoDinheiro.class, factory.criar(" Dinheiro "));
    }
}
