package br.edu.cantina.service;

import br.edu.cantina.dto.LoginRequest;
import br.edu.cantina.dto.RegistroRequest;
import br.edu.cantina.dto.TokenResponse;
import br.edu.cantina.dto.UsuarioResponse;
import br.edu.cantina.exception.CredenciaisInvalidasException;
import br.edu.cantina.exception.EmailJaCadastradoException;
import br.edu.cantina.model.Perfil;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.repository.UsuarioRepository;
import br.edu.cantina.security.ControleTentativasLogin;
import br.edu.cantina.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificador;
    private final JwtService jwtService;
    private final ControleTentativasLogin controleTentativas;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder codificador, JwtService jwtService,
                       ControleTentativasLogin controleTentativas) {
        this.usuarioRepository = usuarioRepository;
        this.codificador = codificador;
        this.jwtService = jwtService;
        this.controleTentativas = controleTentativas;
        this.hashFalso = codificador.encode("senha-inexistente-para-comparacao");
    }

    @Transactional
    public UsuarioResponse registrarCliente(RegistroRequest dados) {
        if (usuarioRepository.existsByEmailIgnoreCase(dados.email().trim())) {
            throw new EmailJaCadastradoException();
        }
        Usuario cliente = new Usuario(dados.nome(), dados.email(), codificador.encode(dados.senha()),
                Perfil.CLIENTE, dados.matricula());
        return UsuarioResponse.de(usuarioRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest dados) {
        String email = dados.email().trim();
        controleTentativas.verificarBloqueio(email);

        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(email);
        String hashParaComparar = encontrado.map(Usuario::getPassword).orElse(hashFalso);
        boolean senhaConfere = codificador.matches(dados.senha(), hashParaComparar);

        if (encontrado.isEmpty() || !senhaConfere || !encontrado.get().isAtivo()) {
            controleTentativas.registrarFalha(email);
            throw new CredenciaisInvalidasException();
        }

        controleTentativas.registrarSucesso(email);
        Usuario usuario = encontrado.get();
        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoSegundos(),
                UsuarioResponse.de(usuario));
    }
}
