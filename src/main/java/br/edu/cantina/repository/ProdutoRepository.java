package br.edu.cantina.repository;

import br.edu.cantina.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    List<Produto> findAllByOrderByNomeAsc();
}
