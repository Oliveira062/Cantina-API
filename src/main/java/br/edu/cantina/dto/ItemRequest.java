package br.edu.cantina.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequest(
        @NotNull(message = "Informe o código do produto.")
        @Positive(message = "Código de produto inválido.")
        Long codigoProduto,

        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade deve ser maior que zero.")
        @Max(value = 50, message = "A quantidade máxima por item é 50.")
        Integer quantidade
) {
}
