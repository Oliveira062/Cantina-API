package br.edu.cantina.observer;

import br.edu.cantina.model.SituacaoPedido;

public record PedidoAlteradoEvento(Long pedidoId, Long clienteId, String nomeCliente, SituacaoPedido situacao,
                                   String resumoItens) {
}
