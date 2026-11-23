package br.edu.cantina.controller;

import br.edu.cantina.dto.LoginRequest;
import br.edu.cantina.dto.RegistroRequest;
import br.edu.cantina.dto.TokenResponse;
import br.edu.cantina.dto.UsuarioResponse;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro de clientes, login e dados do usuário logado")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma conta de cliente")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest dados) {
        return authService.registrarCliente(dados);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e devolve o token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }

    @GetMapping("/eu")
    @Operation(summary = "Mostra os dados do usuário dono do token")
    public UsuarioResponse eu(@AuthenticationPrincipal Usuario usuario) {
        return UsuarioResponse.de(usuario);
    }
}
