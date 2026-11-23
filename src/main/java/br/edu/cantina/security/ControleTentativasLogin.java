package br.edu.cantina.security;

import br.edu.cantina.config.PropriedadesCantina;
import br.edu.cantina.exception.LoginBloqueadoException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ControleTentativasLogin {

    private final Map<String, Tentativas> registros = new ConcurrentHashMap<>();
    private final int maximoTentativas;
    private final Duration tempoBloqueio;

    public ControleTentativasLogin(PropriedadesCantina propriedades) {
        this.maximoTentativas = propriedades.login().maximoTentativas();
        this.tempoBloqueio = Duration.ofMinutes(propriedades.login().bloqueioMinutos());
    }

    public void verificarBloqueio(String email) {
        Tentativas tentativas = registros.get(chave(email));
        if (tentativas == null || tentativas.bloqueadoAte == null) {
            return;
        }
        Instant agora = Instant.now();
        if (agora.isBefore(tentativas.bloqueadoAte)) {
            long minutos = Math.max(1, Duration.between(agora, tentativas.bloqueadoAte).toMinutes() + 1);
            throw new LoginBloqueadoException(minutos);
        }
        registros.remove(chave(email));
    }

    public void registrarFalha(String email) {
        registros.compute(chave(email), (k, atual) -> {
            Tentativas tentativas = atual == null ? new Tentativas() : atual;
            tentativas.falhas++;
            if (tentativas.falhas >= maximoTentativas) {
                tentativas.bloqueadoAte = Instant.now().plus(tempoBloqueio);
            }
            return tentativas;
        });
    }

    public void registrarSucesso(String email) {
        registros.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static class Tentativas {
        private int falhas;
        private Instant bloqueadoAte;
    }
}
