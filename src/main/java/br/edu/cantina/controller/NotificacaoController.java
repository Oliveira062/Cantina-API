package br.edu.cantina.controller;

import br.edu.cantina.dto.NotificacaoResponse;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Avisos de mudança de situação dos pedidos")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    @Operation(summary = "Últimas 50 notificações do usuário logado")
    public List<NotificacaoResponse> listar(@AuthenticationPrincipal Usuario usuario) {
        return notificacaoService.listar(usuario);
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marca uma notificação como lida")
    public NotificacaoResponse marcarComoLida(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return notificacaoService.marcarComoLida(id, usuario);
    }
}
