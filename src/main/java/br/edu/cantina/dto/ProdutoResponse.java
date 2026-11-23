package br.edu.cantina.dto;

import br.edu.cantina.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(Long codigo, String nome, String descricao, BigDecimal preco, int quantidadeEstoque,
                              boolean disponivel, boolean ativo) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getPreco(),
                produto.getQuantidadeEstoque(), produto.isDisponivel(), produto.isAtivo());
    }
}
