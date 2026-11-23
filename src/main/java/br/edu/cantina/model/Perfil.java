package br.edu.cantina.model;

public enum Perfil {
    CLIENTE("Cliente"),
    ATENDENTE("Atendente"),
    COZINHA("Cozinha"),
    ADMIN("Administrador");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFuncionario() {
        return this != CLIENTE;
    }
}
