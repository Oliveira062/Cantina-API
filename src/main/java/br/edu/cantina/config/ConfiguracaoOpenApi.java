package br.edu.cantina.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenApi {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI documentacao() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cantina Universitária - API")
                        .version("1.0.0")
                        .description("Cardápio, pedidos, pagamentos, cozinha e notificações da cantina. "
                                + "Faça login em /api/auth/login, copie o token e clique em Authorize."))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA))
                .components(new Components().addSecuritySchemes(ESQUEMA, new SecurityScheme()
                        .name(ESQUEMA)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
