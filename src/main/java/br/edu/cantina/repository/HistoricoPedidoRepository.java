package br.edu.cantina.repository;

import br.edu.cantina.model.HistoricoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPedidoRepository extends JpaRepository<HistoricoPedido, Long> {

    List<HistoricoPedido> findByPedidoIdOrderByIdAsc(Long pedidoId);
}
