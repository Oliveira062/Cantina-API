package br.edu.cantina.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ConfiguracaoRequest(
        @NotBlank(message = "Informe o nome da cantina.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotNull(message = "Informe a taxa de serviço (0 a 0.30).")
        @DecimalMin(value = "0.00", message = "A taxa de serviço não pode ser negativa.")
        @DecimalMax(value = "0.30", message = "A taxa de serviço máxima é 0.30 (30%).")
        BigDecimal taxaServico,

        @NotNull(message = "Informe o horário de abertura.")
        LocalTime abertura,

        @NotNull(message = "Informe o horário de fechamento.")
        LocalTime fechamento,

        boolean exigirHorario
) {
}
