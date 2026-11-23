package br.edu.cantina.repository;

import br.edu.cantina.model.ConfiguracaoCantina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoRepository extends JpaRepository<ConfiguracaoCantina, Long> {
}
