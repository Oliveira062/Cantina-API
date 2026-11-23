package br.edu.cantina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_destino", length = 20)
    private Perfil perfilDestino;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected Notificacao() {
    }

    private Notificacao(Long usuarioId, Perfil perfilDestino, Long pedidoId, String mensagem) {
        this.usuarioId = usuarioId;
        this.perfilDestino = perfilDestino;
        this.pedidoId = pedidoId;
        this.mensagem = mensagem;
        this.criadaEm = Instant.now();
    }

    public static Notificacao paraUsuario(Long usuarioId, Long pedidoId, String mensagem) {
        return new Notificacao(usuarioId, null, pedidoId, mensagem);
    }

    public static Notificacao paraPerfil(Perfil perfil, Long pedidoId, String mensagem) {
        return new Notificacao(null, perfil, pedidoId, mensagem);
    }

    public boolean destinadaA(Usuario usuario) {
        return usuario.getId().equals(usuarioId) || usuario.getPerfil() == perfilDestino;
    }

    public void marcarComoLida() {
        this.lida = true;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Perfil getPerfilDestino() { return perfilDestino; }
    public Long getPedidoId() { return pedidoId; }
    public String getMensagem() { return mensagem; }
    public boolean isLida() { return lida; }
    public Instant getCriadaEm() { return criadaEm; }
}
