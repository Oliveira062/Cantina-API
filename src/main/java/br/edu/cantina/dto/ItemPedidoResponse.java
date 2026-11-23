package br.edu.cantina.dto;

import br.edu.cantina.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(Long codigoProduto, String produto, int quantidade, BigDecimal precoUnitario,
                                 BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(item.getProduto().getId(), item.getProduto().getNome(), item.getQuantidade(),
                item.getPrecoUnitario(), item.getSubtotal());
    }
}
