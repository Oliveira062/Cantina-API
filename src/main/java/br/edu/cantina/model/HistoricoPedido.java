package br.edu.cantina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "historico_pedidos")
public class HistoricoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_anterior", length = 30)
    private SituacaoPedido situacaoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_nova", nullable = false, length = 30)
    private SituacaoPedido situacaoNova;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    protected HistoricoPedido() {
    }

    public HistoricoPedido(Long pedidoId, SituacaoPedido situacaoAnterior, SituacaoPedido situacaoNova, Usuario usuario) {
        this.pedidoId = pedidoId;
        this.situacaoAnterior = situacaoAnterior;
        this.situacaoNova = situacaoNova;
        this.usuario = usuario;
        this.registradoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public SituacaoPedido getSituacaoAnterior() { return situacaoAnterior; }
    public SituacaoPedido getSituacaoNova() { return situacaoNova; }
    public Usuario getUsuario() { return usuario; }
    public Instant getRegistradoEm() { return registradoEm; }
}
