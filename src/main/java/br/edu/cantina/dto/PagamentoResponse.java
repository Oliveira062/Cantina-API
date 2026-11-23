package br.edu.cantina.dto;

import java.math.BigDecimal;

public record PagamentoResponse(boolean aprovado, String mensagem, BigDecimal troco, PedidoResponse pedido) {
}
