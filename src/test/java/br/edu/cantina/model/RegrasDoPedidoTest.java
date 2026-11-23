package br.edu.cantina.model;

import br.edu.cantina.exception.DadosInvalidosException;
import br.edu.cantina.exception.EstoqueInsuficienteException;
import br.edu.cantina.exception.OperacaoPedidoInvalidaException;
import br.edu.cantina.exception.PedidoVazioException;
import br.edu.cantina.exception.ValorRecebidoInsuficienteException;
import br.edu.cantina.pagamento.PagamentoCartao;
import br.edu.cantina.pagamento.PagamentoDinheiro;
import br.edu.cantina.pagamento.PagamentoPix;
import br.edu.cantina.pagamento.ResultadoPagamento;
import br.edu.cantina.pagamento.TipoPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasDoPedidoTest {

    private static final BigDecimal SEM_TAXA = BigDecimal.ZERO;

    private Produto coxinha;
    private Usuario ana;

    @BeforeEach
    void prepararCenario() {
        coxinha = new Produto("Coxinha", "Frango", new BigDecimal("6.00"), 20);
        ReflectionTestUtils.setField(coxinha, "id", 1L);
        ana = new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, "2024001");
        ReflectionTestUtils.setField(ana, "id", 10L);
    }

    private Pedido pedidoFinalizadoComDuasCoxinhas() {
        Pedido pedido = new Pedido(ana);
        pedido.adicionarItem(coxinha, 2);
        pedido.finalizar();
        return pedido;
    }

    @Test
    @DisplayName("CT01 - produto válido fica disponível")
    void ct01ProdutoValido() {
        Produto suco = new Produto("Suco", "Laranja", new BigDecimal("7.00"), 10);
        assertTrue(suco.isDisponivel());
        assertEquals(10, suco.getQuantidadeEstoque());
    }

    @Test
    @DisplayName("CT02 - preço zero é rejeitado")
    void ct02PrecoInvalido() {
        assertThrows(DadosInvalidosException.class, () -> new Produto("Bala", "Menta", BigDecimal.ZERO, 5));
    }

    @Test
    @DisplayName("CT03 - mesmo produto adicionado duas vezes vira um item só")
    void ct03AdicionarItem() {
        Pedido pedido = new Pedido(ana);
        pedido.adicionarItem(coxinha, 2);
        pedido.adicionarItem(coxinha, 1);
        assertEquals(1, pedido.getItens().size());
        assertEquals(0, new BigDecimal("18.00").compareTo(pedido.calcularTotal(SEM_TAXA)));
    }

    @Test
    @DisplayName("CT04 - quantidade acima do estoque é rejeitada")
    void ct04EstoqueInsuficiente() {
        Pedido pedido = new Pedido(ana);
        assertThrows(EstoqueInsuficienteException.class, () -> pedido.adicionarItem(coxinha, 25));
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    @DisplayName("CT05 - pedido vazio não pode ser finalizado")
    void ct05PedidoVazio() {
        Pedido pedido = new Pedido(ana);
        assertThrows(PedidoVazioException.class, pedido::finalizar);
        assertEquals(SituacaoPedido.CRIADO, pedido.getSituacao());
    }

    @Test
    @DisplayName("CT06 - pagamento por PIX baixa o estoque")
    void ct06PagarPix() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        pedido.registrarPagamento(new PagamentoPix(), null, SEM_TAXA);
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
        assertEquals(18, coxinha.getQuantidadeEstoque());
    }

    @Test
    @DisplayName("CT07 - pagamento no cartão registra o tipo usado")
    void ct07PagarCartao() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        pedido.registrarPagamento(new PagamentoCartao(), null, SEM_TAXA);
        assertEquals(TipoPagamento.CARTAO, pedido.getTipoPagamento());
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
    }

    @Test
    @DisplayName("CT08 - dinheiro insuficiente é rejeitado e o troco é calculado")
    void ct08DinheiroInsuficiente() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        assertThrows(ValorRecebidoInsuficienteException.class,
                () -> pedido.registrarPagamento(new PagamentoDinheiro(), new BigDecimal("5.00"), SEM_TAXA));
        assertEquals(SituacaoPedido.AGUARDANDO_PAGAMENTO, pedido.getSituacao());
        assertEquals(20, coxinha.getQuantidadeEstoque());

        ResultadoPagamento resultado =
                pedido.registrarPagamento(new PagamentoDinheiro(), new BigDecimal("20.00"), SEM_TAXA);
        assertEquals(0, new BigDecimal("8.00").compareTo(resultado.troco()));
    }

    @Test
    @DisplayName("CT10 - mudanças de situação seguem a ordem do ciclo de vida")
    void ct10CicloDeVida() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        pedido.registrarPagamento(new PagamentoPix(), null, SEM_TAXA);
        pedido.alterarSituacao(SituacaoPedido.EM_PREPARACAO);
        pedido.alterarSituacao(SituacaoPedido.PRONTO);
        assertThrows(OperacaoPedidoInvalidaException.class, () -> pedido.alterarSituacao(SituacaoPedido.PAGO));
        assertEquals(SituacaoPedido.PRONTO, pedido.getSituacao());
    }

    @Test
    @DisplayName("CT11 - pedido pago ainda não pode ser entregue")
    void ct11EntregaAntesDaHora() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        pedido.registrarPagamento(new PagamentoPix(), null, SEM_TAXA);
        assertThrows(OperacaoPedidoInvalidaException.class, () -> pedido.alterarSituacao(SituacaoPedido.ENTREGUE));
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
    }

    @Test
    @DisplayName("CT12 - cancelar pedido pago devolve o estoque; entregue não cancela")
    void ct12Cancelamento() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        pedido.registrarPagamento(new PagamentoPix(), null, SEM_TAXA);
        assertTrue(pedido.cancelar());
        assertEquals(SituacaoPedido.CANCELADO, pedido.getSituacao());
        assertEquals(20, coxinha.getQuantidadeEstoque());

        Pedido entregue = pedidoFinalizadoComDuasCoxinhas();
        entregue.registrarPagamento(new PagamentoPix(), null, SEM_TAXA);
        entregue.alterarSituacao(SituacaoPedido.EM_PREPARACAO);
        entregue.alterarSituacao(SituacaoPedido.PRONTO);
        entregue.alterarSituacao(SituacaoPedido.ENTREGUE);
        assertThrows(OperacaoPedidoInvalidaException.class, entregue::cancelar);
    }

    @Test
    @DisplayName("Taxa de serviço é aplicada e o total fica fixo depois do pagamento")
    void taxaDeServico() {
        Pedido pedido = pedidoFinalizadoComDuasCoxinhas();
        assertEquals(0, new BigDecimal("13.20").compareTo(pedido.calcularTotal(new BigDecimal("0.10"))));
        pedido.registrarPagamento(new PagamentoPix(), null, new BigDecimal("0.10"));
        assertEquals(0, new BigDecimal("13.20").compareTo(pedido.calcularTotal(new BigDecimal("0.25"))));
    }

    @Test
    @DisplayName("Produto desativado não pode entrar em pedido")
    void produtoDesativado() {
        coxinha.setAtivo(false);
        assertFalse(coxinha.isDisponivel());
        assertThrows(EstoqueInsuficienteException.class, () -> new Pedido(ana).adicionarItem(coxinha, 1));
    }
}
