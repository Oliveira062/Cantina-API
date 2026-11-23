package br.edu.cantina.controller;

import br.edu.cantina.dto.AtualizarProdutoRequest;
import br.edu.cantina.dto.EstoqueRequest;
import br.edu.cantina.dto.PrecoRequest;
import br.edu.cantina.dto.ProdutoRequest;
import br.edu.cantina.dto.ProdutoResponse;
import br.edu.cantina.model.Perfil;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Cardápio", description = "Consulta pública do cardápio e manutenção pelo administrador")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Lista o cardápio (o administrador também vê os produtos desativados)")
    public List<ProdutoResponse> listar(@AuthenticationPrincipal Usuario usuario) {
        return produtoService.listarCardapio(ehAdmin(usuario));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Consulta um produto pelo código")
    public ProdutoResponse consultar(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        return produtoService.consultar(codigo, ehAdmin(usuario));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um produto")
    public ProdutoResponse cadastrar(@Valid @RequestBody ProdutoRequest dados) {
        return produtoService.cadastrar(dados);
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Altera nome e descrição")
    public ProdutoResponse atualizar(@PathVariable Long codigo, @Valid @RequestBody AtualizarProdutoRequest dados) {
        return produtoService.atualizarDados(codigo, dados);
    }

    @PatchMapping("/{codigo}/preco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Altera o preço")
    public ProdutoResponse atualizarPreco(@PathVariable Long codigo, @Valid @RequestBody PrecoRequest dados) {
        return produtoService.atualizarPreco(codigo, dados.preco());
    }

    @PostMapping("/{codigo}/estoque")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Repõe o estoque")
    public ProdutoResponse reporEstoque(@PathVariable Long codigo, @Valid @RequestBody EstoqueRequest dados) {
        return produtoService.reporEstoque(codigo, dados.quantidade());
    }

    @DeleteMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativa o produto (ele sai do cardápio, mas continua nos pedidos antigos)")
    public ProdutoResponse desativar(@PathVariable Long codigo) {
        return produtoService.alterarAtivo(codigo, false);
    }

    @PostMapping("/{codigo}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reativa um produto desativado")
    public ProdutoResponse reativar(@PathVariable Long codigo) {
        return produtoService.alterarAtivo(codigo, true);
    }

    private boolean ehAdmin(Usuario usuario) {
        return usuario != null && usuario.getPerfil() == Perfil.ADMIN;
    }
}
