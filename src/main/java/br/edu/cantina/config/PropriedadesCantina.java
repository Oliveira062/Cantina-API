package br.edu.cantina.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cantina")
public record PropriedadesCantina(String fusoHorario, Jwt jwt, Login login, Admin admin, Cors cors) {

    public record Jwt(String segredo, long expiracaoMinutos, String emissor) {
    }

    public record Login(int maximoTentativas, long bloqueioMinutos) {
    }

    public record Admin(String email, String senha) {
    }

    public record Cors(List<String> origensPermitidas) {
    }
}
