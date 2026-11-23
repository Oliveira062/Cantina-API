package br.edu.cantina.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueRequest(
        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade de reposição deve ser maior que zero.")
        @Max(value = 10000, message = "A reposição máxima por vez é 10000.")
        Integer quantidade
) {
}
