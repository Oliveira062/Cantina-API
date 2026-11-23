package br.edu.cantina.model;

import br.edu.cantina.exception.EstoqueInsuficienteException;
import br.edu.cantina.exception.OperacaoPedidoInvalidaException;
import br.edu.cantina.exception.PedidoVazioException;
import br.edu.cantina.exception.ProdutoNaoEncontradoException;
import br.edu.cantina.pagamento.EstrategiaPagamento;
import br.edu.cantina.pagamento.ResultadoPagamento;
import br.edu.cantina.pagamento.TipoPagamento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoPedido situacao = SituacaoPedido.CRIADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", length = 20)
    private TipoPagamento tipoPagamento;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal troco = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    private Long versao;

    protected Pedido() {
    }

    public Pedido(Usuario cliente) {
        this.cliente = Objects.requireNonNull(cliente, "Todo pedido precisa de um cliente.");
        this.criadoEm = Instant.now();
        this.atualizadoEm = criadoEm;
    }

    public void adicionarItem(Produto produto, int quantidade) {
        exigirSituacao(SituacaoPedido.CRIADO, "adicionar itens");
        if (quantidade <= 0) {
            throw new EstoqueInsuficienteException("A quantidade deve ser maior que zero.");
        }
        if (!produto.isDisponivel()) {
            throw new EstoqueInsuficienteException("O produto " + produto.getNome() + " está indisponível no momento.");
        }
        ItemPedido existente = buscarItem(produto.getId());
        int quantidadeFinal = quantidade + (existente == null ? 0 : existente.getQuantidade());
        if (!produto.temEstoque(quantidadeFinal)) {
            throw new EstoqueInsuficienteException(produto.getNome(), quantidadeFinal, produto.getQuantidadeEstoque());
        }

        if (existente == null) {
            itens.add(new ItemPedido(this, produto, quantidade));
        } else {
            existente.aumentarQuantidade(quantidade);
        }
        tocar();
    }

    public void removerItem(Long codigoProduto) {
        exigirSituacao(SituacaoPedido.CRIADO, "remover itens");
        ItemPedido item = buscarItem(codigoProduto);
        if (item == null) {
            throw new ProdutoNaoEncontradoException(
                    String.format("O produto de código %d não está no pedido nº %d.", codigoProduto, id));
        }
        itens.remove(item);
        tocar();
    }

    public BigDecimal getSubtotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            soma = soma.add(item.getSubtotal());
        }
        return soma;
    }

    public BigDecimal calcularTotal(BigDecimal taxaServico) {
        if (valorTotal != null) {
            return valorTotal;
        }
        BigDecimal subtotal = getSubtotal();
        return subtotal.add(subtotal.multiply(taxaServico)).setScale(2, RoundingMode.HALF_UP);
    }

    public void finalizar() {
        exigirSituacao(SituacaoPedido.CRIADO, "finalizar");
        if (itens.isEmpty()) {
            throw new PedidoVazioException(id);
        }
        validarEstoqueDeTodosOsItens();
        alterarSituacao(SituacaoPedido.AGUARDANDO_PAGAMENTO);
    }

    public ResultadoPagamento registrarPagamento(EstrategiaPagamento estrategia, BigDecimal valorRecebido,
                                                 BigDecimal taxaServico) {
        if (itens.isEmpty()) {
            throw new PedidoVazioException(id);
        }
        exigirSituacao(SituacaoPedido.AGUARDANDO_PAGAMENTO, "pagar");
        validarEstoqueDeTodosOsItens();

        BigDecimal total = calcularTotal(taxaServico);
        ResultadoPagamento resultado = estrategia.processar(total, valorRecebido);
        if (!resultado.aprovado()) {
            return resultado;
        }

        for (ItemPedido item : itens) {
            item.getProduto().baixarEstoque(item.getQuantidade());
        }
        this.tipoPagamento = estrategia.getTipo();
        this.valorTotal = total;
        this.troco = resultado.troco();
        alterarSituacao(SituacaoPedido.PAGO);
        return resultado;
    }

    public void alterarSituacao(SituacaoPedido nova) {
        if (!situacao.podeIrPara(nova)) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "Não é possível mudar o pedido nº %d de \"%s\" para \"%s\".",
                    id, situacao.getDescricao(), nova.getDescricao()));
        }
        situacao = nova;
        tocar();
    }

    public boolean cancelar() {
        if (!situacao.podeIrPara(SituacaoPedido.CANCELADO)) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "O pedido nº %d está \"%s\" e não pode ser cancelado.", id, situacao.getDescricao()));
        }
        boolean devolverEstoque = situacao == SituacaoPedido.PAGO;
        if (devolverEstoque) {
            for (ItemPedido item : itens) {
                item.getProduto().devolverEstoque(item.getQuantidade());
            }
        }
        alterarSituacao(SituacaoPedido.CANCELADO);
        return devolverEstoque;
    }

    public boolean pertenceA(Usuario usuario) {
        return cliente.getId() != null && cliente.getId().equals(usuario.getId());
    }

    private void exigirSituacao(SituacaoPedido esperada, String acao) {
        if (situacao != esperada) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "Só é possível %s quando o pedido está \"%s\" (situação atual: \"%s\").",
                    acao, esperada.getDescricao(), situacao.getDescricao()));
        }
    }

    private void validarEstoqueDeTodosOsItens() {
        for (ItemPedido item : itens) {
            Produto p = item.getProduto();
            if (!p.temEstoque(item.getQuantidade())) {
                throw new EstoqueInsuficienteException(p.getNome(), item.getQuantidade(), p.getQuantidadeEstoque());
            }
        }
    }

    private ItemPedido buscarItem(Long codigoProduto) {
        for (ItemPedido item : itens) {
            if (item.getProduto().getId().equals(codigoProduto)) {
                return item;
            }
        }
        return null;
    }

    private void tocar() {
        this.atualizadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public SituacaoPedido getSituacao() { return situacao; }
    public TipoPagamento getTipoPagamento() { return tipoPagamento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getTroco() { return troco; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
