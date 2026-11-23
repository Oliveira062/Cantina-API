package br.edu.cantina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarProdutoRequest(
        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
        String nome,

        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
        String descricao
) {
}
