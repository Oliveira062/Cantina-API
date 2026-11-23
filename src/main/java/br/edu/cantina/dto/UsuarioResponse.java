package br.edu.cantina.dto;

import br.edu.cantina.model.Perfil;
import br.edu.cantina.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, String matricula, boolean ativo) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
                usuario.getMatricula(), usuario.isAtivo());
    }
}
