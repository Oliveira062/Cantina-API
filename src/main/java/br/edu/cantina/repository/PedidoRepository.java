package br.edu.cantina.repository;

import br.edu.cantina.model.Pedido;
import br.edu.cantina.model.SituacaoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);

    List<Pedido> findByClienteIdAndSituacaoOrderByIdDesc(Long clienteId, SituacaoPedido situacao);

    List<Pedido> findBySituacaoOrderByIdDesc(SituacaoPedido situacao);

    List<Pedido> findBySituacaoInOrderByIdAsc(Collection<SituacaoPedido> situacoes);

    List<Pedido> findAllByOrderByIdDesc();
}
