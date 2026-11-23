package br.edu.cantina.service;

import br.edu.cantina.dto.FuncionarioRequest;
import br.edu.cantina.dto.UsuarioResponse;
import br.edu.cantina.exception.DadosInvalidosException;
import br.edu.cantina.exception.EmailJaCadastradoException;
import br.edu.cantina.exception.RecursoNaoEncontradoException;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder codificador;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder codificador) {
        this.repository = repository;
        this.codificador = codificador;
    }

    @Transactional
    public UsuarioResponse criarFuncionario(FuncionarioRequest dados) {
        if (repository.existsByEmailIgnoreCase(dados.email().trim())) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(dados.nome(), dados.email(), codificador.encode(dados.senha()),
                dados.perfil(), null);
        return UsuarioResponse.de(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional
    public UsuarioResponse alterarStatus(Long id, boolean ativo, Usuario quemAltera) {
        if (id.equals(quemAltera.getId()) && !ativo) {
            throw new DadosInvalidosException("Você não pode desativar a própria conta.");
        }
        Usuario usuario = buscar(id);
        usuario.setAtivo(ativo);
        return UsuarioResponse.de(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário de código " + id + " não encontrado."));
    }
}
