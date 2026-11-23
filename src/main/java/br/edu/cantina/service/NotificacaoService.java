package br.edu.cantina.service;

import br.edu.cantina.dto.NotificacaoResponse;
import br.edu.cantina.exception.RecursoNaoEncontradoException;
import br.edu.cantina.model.Notificacao;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(Usuario usuario) {
        return repository.findTop50ByUsuarioIdOrPerfilDestinoOrderByIdDesc(usuario.getId(), usuario.getPerfil())
                .stream().map(NotificacaoResponse::de).toList();
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long id, Usuario usuario) {
        Notificacao notificacao = repository.findById(id)
                .filter(n -> n.destinadaA(usuario))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada."));
        notificacao.marcarComoLida();
        return NotificacaoResponse.de(notificacao);
    }
}
