package br.edu.cantina.controller;

import br.edu.cantina.dto.ConfiguracaoRequest;
import br.edu.cantina.dto.ConfiguracaoResponse;
import br.edu.cantina.service.ConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cantina")
@Tag(name = "Cantina", description = "Nome, horário de funcionamento e taxa de serviço")
public class CantinaController {

    private final ConfiguracaoService configuracaoService;

    public CantinaController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @GetMapping
    @Operation(summary = "Mostra os dados da cantina e se ela está aberta agora")
    public ConfiguracaoResponse consultar() {
        return configuracaoService.consultar();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Altera os dados da cantina")
    public ConfiguracaoResponse atualizar(@Valid @RequestBody ConfiguracaoRequest dados) {
        return configuracaoService.atualizar(dados);
    }
}
