package br.edu.cantina.controller;

import br.edu.cantina.dto.PedidoResponse;
import br.edu.cantina.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cozinha")
@Tag(name = "Cozinha", description = "Fila de preparo")
public class CozinhaController {

    private final PedidoService pedidoService;

    public CozinhaController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/fila")
    @PreAuthorize("hasAnyRole('COZINHA', 'ADMIN')")
    @Operation(summary = "Pedidos pagos e em preparação, do mais antigo para o mais novo")
    public List<PedidoResponse> fila() {
        return pedidoService.filaDaCozinha();
    }
}
