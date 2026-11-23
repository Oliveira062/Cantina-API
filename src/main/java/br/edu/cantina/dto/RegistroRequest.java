package br.edu.cantina.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail em formato inválido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha precisa ter letras e números.")
        String senha,

        @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres.")
        @Pattern(regexp = "^[0-9]*$", message = "A matrícula deve conter apenas números.")
        String matricula
) {
}
