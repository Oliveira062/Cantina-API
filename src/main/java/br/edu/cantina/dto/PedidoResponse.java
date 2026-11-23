package br.edu.cantina.dto;

import br.edu.cantina.model.Pedido;
import br.edu.cantina.model.SituacaoPedido;
import br.edu.cantina.pagamento.TipoPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(Long numero, Long clienteId, String cliente, SituacaoPedido situacao,
                             String situacaoDescricao, List<ItemPedidoResponse> itens, BigDecimal subtotal,
                             BigDecimal total, TipoPagamento pagamento, BigDecimal troco, Instant criadoEm,
                             Instant atualizadoEm) {

    public static PedidoResponse de(Pedido pedido, BigDecimal taxaServico) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream().map(ItemPedidoResponse::de).toList();
        return new PedidoResponse(pedido.getId(), pedido.getCliente().getId(), pedido.getCliente().getNome(),
                pedido.getSituacao(), pedido.getSituacao().getDescricao(), itens, pedido.getSubtotal(),
                pedido.calcularTotal(taxaServico), pedido.getTipoPagamento(), pedido.getTroco(),
                pedido.getCriadoEm(), pedido.getAtualizadoEm());
    }
}
