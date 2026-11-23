package br.edu.cantina.dto;

import br.edu.cantina.model.Notificacao;

import java.time.Instant;

public record NotificacaoResponse(Long id, Long pedido, String mensagem, boolean lida, Instant criadaEm) {

    public static NotificacaoResponse de(Notificacao notificacao) {
        return new NotificacaoResponse(notificacao.getId(), notificacao.getPedidoId(), notificacao.getMensagem(),
                notificacao.isLida(), notificacao.getCriadaEm());
    }
}
