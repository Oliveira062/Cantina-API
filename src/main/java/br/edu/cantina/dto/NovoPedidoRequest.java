package br.edu.cantina.dto;

import jakarta.validation.constraints.Positive;

public record NovoPedidoRequest(
        @Positive(message = "Código de cliente inválido.")
        Long clienteId
) {
}
