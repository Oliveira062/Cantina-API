package br.edu.cantina.dto;

public record CancelamentoResponse(boolean estoqueDevolvido, PedidoResponse pedido) {
}
