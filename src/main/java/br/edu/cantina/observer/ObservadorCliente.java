package br.edu.cantina.observer;

import br.edu.cantina.model.Notificacao;
import br.edu.cantina.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ObservadorCliente implements ObservadorPedido {

    private static final Logger log = LoggerFactory.getLogger(ObservadorCliente.class);

    private final NotificacaoRepository notificacaoRepository;

    public ObservadorCliente(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizar(PedidoAlteradoEvento evento) {
        try {
            String mensagem = String.format("Olá, %s! Seu pedido nº %d agora está: %s.",
                    evento.nomeCliente(), evento.pedidoId(), evento.situacao().getDescricao());
            notificacaoRepository.save(Notificacao.paraUsuario(evento.clienteId(), evento.pedidoId(), mensagem));
        } catch (RuntimeException e) {
            log.warn("Não foi possível notificar o cliente do pedido nº {}: {}", evento.pedidoId(), e.getMessage());
        }
    }
}
