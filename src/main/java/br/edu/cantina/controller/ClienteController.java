package br.edu.cantina.controller;

import br.edu.cantina.dto.UsuarioResponse;
import br.edu.cantina.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
@Tag(name = "Clientes", description = "Busca de clientes para abrir pedidos no balcão")
public class ClienteController {

    private final UsuarioService usuarioService;

    public ClienteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Busca clientes ativos por nome, e-mail ou matrícula (até 20 resultados)")
    public List<UsuarioResponse> buscar(@RequestParam(required = false) String busca) {
        return usuarioService.buscarClientes(busca);
    }
}
