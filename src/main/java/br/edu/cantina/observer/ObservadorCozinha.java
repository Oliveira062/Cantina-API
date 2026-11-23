package br.edu.cantina.observer;

import br.edu.cantina.model.Notificacao;
import br.edu.cantina.model.Perfil;
import br.edu.cantina.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ObservadorCozinha implements ObservadorPedido {

    private static final Logger log = LoggerFactory.getLogger(ObservadorCozinha.class);

    private final NotificacaoRepository notificacaoRepository;

    public ObservadorCozinha(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizar(PedidoAlteradoEvento evento) {
        String mensagem = switch (evento.situacao()) {
            case PAGO -> "Novo pedido nº " + evento.pedidoId() + " para preparar: " + evento.resumoItens();
            case CANCELADO -> "Pedido nº " + evento.pedidoId() + " foi cancelado.";
            default -> null;
        };
        if (mensagem == null) {
            return;
        }
        try {
            notificacaoRepository.save(Notificacao.paraPerfil(Perfil.COZINHA, evento.pedidoId(), mensagem));
        } catch (RuntimeException e) {
            log.warn("Não foi possível avisar a cozinha sobre o pedido nº {}: {}", evento.pedidoId(), e.getMessage());
        }
    }
}
