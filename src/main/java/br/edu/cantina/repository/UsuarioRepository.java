package br.edu.cantina.repository;

import br.edu.cantina.model.Perfil;
import br.edu.cantina.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);

    List<Usuario> findAllByOrderByNomeAsc();

    @Query("""
            select u from Usuario u
            where u.perfil = br.edu.cantina.model.Perfil.CLIENTE and u.ativo = true
              and (lower(u.nome) like lower(concat('%', :busca, '%'))
                   or lower(u.email) like lower(concat('%', :busca, '%'))
                   or u.matricula like concat('%', :busca, '%'))
            order by u.nome
            """)
    List<Usuario> buscarClientesAtivos(@Param("busca") String busca);
}
