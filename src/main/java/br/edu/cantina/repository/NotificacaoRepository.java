package br.edu.cantina.repository;

import br.edu.cantina.model.Notificacao;
import br.edu.cantina.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop50ByUsuarioIdOrPerfilDestinoOrderByIdDesc(Long usuarioId, Perfil perfilDestino);
}
