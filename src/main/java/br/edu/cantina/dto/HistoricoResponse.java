package br.edu.cantina.dto;

import br.edu.cantina.model.HistoricoPedido;
import br.edu.cantina.model.SituacaoPedido;

import java.time.Instant;

public record HistoricoResponse(SituacaoPedido de, SituacaoPedido para, String responsavel, Instant registradoEm) {

    public static HistoricoResponse de(HistoricoPedido historico) {
        return new HistoricoResponse(historico.getSituacaoAnterior(), historico.getSituacaoNova(),
                historico.getUsuario().getNome(), historico.getRegistradoEm());
    }
}
