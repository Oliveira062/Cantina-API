package br.edu.cantina.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {
}
