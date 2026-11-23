package br.edu.cantina.dto;

import jakarta.validation.constraints.NotNull;

public record StatusUsuarioRequest(
        @NotNull(message = "Informe se o usuário fica ativo.")
        Boolean ativo
) {
}
