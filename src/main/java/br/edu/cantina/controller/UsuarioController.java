package br.edu.cantina.controller;

import br.edu.cantina.dto.FuncionarioRequest;
import br.edu.cantina.dto.StatusUsuarioRequest;
import br.edu.cantina.dto.UsuarioResponse;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários", description = "Gestão de contas, exclusiva do administrador")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria conta de funcionário (atendente, cozinha ou administrador)")
    public UsuarioResponse criar(@Valid @RequestBody FuncionarioRequest dados) {
        return usuarioService.criarFuncionario(dados);
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ativa ou desativa uma conta")
    public UsuarioResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusUsuarioRequest dados,
                                         @AuthenticationPrincipal Usuario administrador) {
        return usuarioService.alterarStatus(id, dados.ativo(), administrador);
    }
}
