package br.edu.cantina.pagamento;

import br.edu.cantina.exception.PagamentoInvalidoException;

import java.text.Normalizer;

public enum TipoPagamento {
    PIX("PIX"),
    CARTAO("Cartão"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    TipoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoPagamento de(String texto) {
        if (texto == null || texto.isBlank()) {
            throw invalido(texto);
        }
        String normalizado = Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        for (TipoPagamento tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        throw invalido(texto);
    }

    private static PagamentoInvalidoException invalido(String texto) {
        return new PagamentoInvalidoException(String.format(
                "Forma de pagamento \"%s\" inválida. Escolha PIX, CARTAO ou DINHEIRO.", texto == null ? "" : texto));
    }
}
