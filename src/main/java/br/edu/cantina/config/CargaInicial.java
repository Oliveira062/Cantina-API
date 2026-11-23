package br.edu.cantina.config;

import br.edu.cantina.model.Perfil;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CargaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificador;
    private final PropriedadesCantina propriedades;

    public CargaInicial(UsuarioRepository usuarioRepository, PasswordEncoder codificador,
                        PropriedadesCantina propriedades) {
        this.usuarioRepository = usuarioRepository;
        this.codificador = codificador;
        this.propriedades = propriedades;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByPerfil(Perfil.ADMIN)) {
            return;
        }
        String email = propriedades.admin().email();
        String senha = propriedades.admin().senha();
        if (email == null || email.isBlank() || senha == null || senha.length() < 8) {
            log.warn("Nenhum administrador cadastrado. Defina ADMIN_EMAIL e ADMIN_SENHA (mínimo 8 caracteres) "
                    + "e reinicie a aplicação.");
            return;
        }
        usuarioRepository.save(new Usuario("Administrador", email, codificador.encode(senha), Perfil.ADMIN, null));
        log.info("Administrador inicial criado: {}", email.trim().toLowerCase());
    }
}
