package br.edu.cantina.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ConfiguracaoResponse(String nome, BigDecimal taxaServico, LocalTime abertura, LocalTime fechamento,
                                   boolean exigirHorario, boolean abertaAgora) {
}
