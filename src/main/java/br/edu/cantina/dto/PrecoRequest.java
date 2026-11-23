package br.edu.cantina.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PrecoRequest(
        @NotNull(message = "Informe o novo preço.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @DecimalMax(value = "9999.99", message = "O preço máximo é 9999,99.")
        @Digits(integer = 4, fraction = 2, message = "Use no máximo duas casas decimais.")
        BigDecimal preco
) {
}
