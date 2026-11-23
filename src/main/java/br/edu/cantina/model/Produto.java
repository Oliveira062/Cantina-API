package br.edu.cantina.model;

import br.edu.cantina.exception.DadosInvalidosException;
import br.edu.cantina.exception.EstoqueInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEstoque;

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    private Long versao;

    protected Produto() {
    }

    public Produto(String nome, String descricao, BigDecimal preco, int quantidadeInicial) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do produto é obrigatório.");
        }
        validarPreco(preco);
        if (quantidadeInicial < 0) {
            throw new DadosInvalidosException("A quantidade inicial não pode ser negativa.");
        }
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.preco = preco;
        this.quantidadeEstoque = quantidadeInicial;
    }

    private static void validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DadosInvalidosException("O preço deve ser maior que zero.");
        }
    }

    public boolean isDisponivel() {
        return ativo && quantidadeEstoque > 0;
    }

    public boolean temEstoque(int quantidade) {
        return quantidade > 0 && quantidade <= quantidadeEstoque;
    }

    public void baixarEstoque(int quantidade) {
        if (!temEstoque(quantidade)) {
            throw new EstoqueInsuficienteException(nome, quantidade, quantidadeEstoque);
        }
        quantidadeEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade de reposição deve ser maior que zero.");
        }
        quantidadeEstoque += quantidade;
    }

    void devolverEstoque(int quantidade) {
        quantidadeEstoque += quantidade;
    }

    public void atualizarPreco(BigDecimal novoPreco) {
        validarPreco(novoPreco);
        this.preco = novoPreco;
    }

    public void atualizarDados(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do produto é obrigatório.");
        }
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public boolean isAtivo() { return ativo; }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
