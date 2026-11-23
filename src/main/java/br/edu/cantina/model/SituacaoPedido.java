package br.edu.cantina.model;

public enum SituacaoPedido {
    CRIADO("Criado"),
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    PAGO("Pago"),
    EM_PREPARACAO("Em preparação"),
    PRONTO("Pronto"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    SituacaoPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean podeIrPara(SituacaoPedido nova) {
        return switch (this) {
            case CRIADO -> nova == AGUARDANDO_PAGAMENTO || nova == CANCELADO;
            case AGUARDANDO_PAGAMENTO -> nova == PAGO || nova == CANCELADO;
            case PAGO -> nova == EM_PREPARACAO || nova == CANCELADO;
            case EM_PREPARACAO -> nova == PRONTO || nova == CANCELADO;
            case PRONTO -> nova == ENTREGUE || nova == CANCELADO;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
