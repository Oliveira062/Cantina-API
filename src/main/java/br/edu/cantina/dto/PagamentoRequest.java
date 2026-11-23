package br.edu.cantina.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PagamentoRequest(
        @NotBlank(message = "Informe a forma de pagamento: PIX, CARTAO ou DINHEIRO.")
        String tipo,

        @DecimalMin(value = "0.00", message = "O valor recebido não pode ser negativo.")
        @Digits(integer = 6, fraction = 2, message = "Use no máximo duas casas decimais.")
        BigDecimal valorRecebido
) {
}
