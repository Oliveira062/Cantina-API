package br.edu.cantina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "configuracao_cantina")
public class ConfiguracaoCantina {

    public static final Long ID_UNICO = 1L;

    @Id
    private Long id = ID_UNICO;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "taxa_servico", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxaServico;

    @Column(nullable = false)
    private LocalTime abertura;

    @Column(nullable = false)
    private LocalTime fechamento;

    @Column(name = "exigir_horario", nullable = false)
    private boolean exigirHorario;

    protected ConfiguracaoCantina() {
    }

    public boolean estaAberta(LocalTime hora) {
        return !hora.isBefore(abertura) && hora.isBefore(fechamento);
    }

    public void atualizar(String nome, BigDecimal taxaServico, LocalTime abertura, LocalTime fechamento,
                          boolean exigirHorario) {
        this.nome = nome.trim();
        this.taxaServico = taxaServico;
        this.abertura = abertura;
        this.fechamento = fechamento;
        this.exigirHorario = exigirHorario;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getTaxaServico() { return taxaServico; }
    public LocalTime getAbertura() { return abertura; }
    public LocalTime getFechamento() { return fechamento; }
    public boolean isExigirHorario() { return exigirHorario; }
}
