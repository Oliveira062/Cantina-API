package br.edu.cantina.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
        String nome,

        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
        String descricao,

        @NotNull(message = "Informe o preço.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @DecimalMax(value = "9999.99", message = "O preço máximo é 9999,99.")
        @Digits(integer = 4, fraction = 2, message = "Use no máximo duas casas decimais.")
        BigDecimal preco,

        @NotNull(message = "Informe a quantidade inicial.")
        @Min(value = 0, message = "A quantidade inicial não pode ser negativa.")
        @Max(value = 10000, message = "A quantidade máxima é 10000.")
        Integer quantidadeInicial
) {
}
