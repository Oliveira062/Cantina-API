package br.edu.cantina.dto;

import java.time.Instant;
import java.util.Map;

public record ErroResponse(Instant momento, int status, String erro, String mensagem, String caminho,
                           Map<String, String> campos) {
}
