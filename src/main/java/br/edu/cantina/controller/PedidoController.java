package br.edu.cantina.controller;

import br.edu.cantina.dto.CancelamentoResponse;
import br.edu.cantina.dto.HistoricoResponse;
import br.edu.cantina.dto.ItemRequest;
import br.edu.cantina.dto.NovoPedidoRequest;
import br.edu.cantina.dto.PagamentoRequest;
import br.edu.cantina.dto.PagamentoResponse;
import br.edu.cantina.dto.PedidoResponse;
import br.edu.cantina.model.SituacaoPedido;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Ciclo completo do pedido, do carrinho à entrega")
public class PedidoController {

    private static final String QUEM_MONTA_PEDIDO = "hasAnyRole('CLIENTE', 'ATENDENTE', 'ADMIN')";

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Abre um pedido (cliente para si; atendente informa o clienteId)")
    public PedidoResponse criar(@Valid @RequestBody(required = false) NovoPedidoRequest dados,
                                @AuthenticationPrincipal Usuario usuario) {
        Long clienteId = dados == null ? null : dados.clienteId();
        return pedidoService.criar(clienteId, usuario);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos (o cliente vê apenas os próprios)")
    public List<PedidoResponse> listar(@RequestParam(required = false) SituacaoPedido situacao,
                                       @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.listar(situacao, usuario);
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Consulta um pedido")
    public PedidoResponse consultar(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.consultar(numero, usuario);
    }

    @GetMapping("/{numero}/historico")
    @Operation(summary = "Mostra todas as mudanças de situação do pedido")
    public List<HistoricoResponse> historico(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.historico(numero, usuario);
    }

    @PostMapping("/{numero}/itens")
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Adiciona um item ao pedido")
    public PedidoResponse adicionarItem(@PathVariable Long numero, @Valid @RequestBody ItemRequest dados,
                                        @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.adicionarItem(numero, dados.codigoProduto(), dados.quantidade(), usuario);
    }

    @DeleteMapping("/{numero}/itens/{codigoProduto}")
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Remove um item do pedido")
    public PedidoResponse removerItem(@PathVariable Long numero, @PathVariable Long codigoProduto,
                                      @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.removerItem(numero, codigoProduto, usuario);
    }

    @PostMapping("/{numero}/finalizar")
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Fecha o carrinho e deixa o pedido aguardando pagamento")
    public PedidoResponse finalizar(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.finalizar(numero, usuario);
    }

    @PostMapping("/{numero}/pagamento")
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Registra o pagamento (PIX, CARTAO ou DINHEIRO)")
    public PagamentoResponse pagar(@PathVariable Long numero, @Valid @RequestBody PagamentoRequest dados,
                                   @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.pagar(numero, dados.tipo(), dados.valorRecebido(), usuario);
    }

    @PostMapping("/{numero}/preparo")
    @PreAuthorize("hasAnyRole('COZINHA', 'ADMIN')")
    @Operation(summary = "A cozinha começa a preparar")
    public PedidoResponse iniciarPreparo(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.iniciarPreparo(numero, usuario);
    }

    @PostMapping("/{numero}/pronto")
    @PreAuthorize("hasAnyRole('COZINHA', 'ADMIN')")
    @Operation(summary = "A cozinha avisa que está pronto")
    public PedidoResponse marcarPronto(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.marcarPronto(numero, usuario);
    }

    @PostMapping("/{numero}/entrega")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    @Operation(summary = "O atendente confirma a entrega")
    public PedidoResponse confirmarEntrega(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.confirmarEntrega(numero, usuario);
    }

    @PostMapping("/{numero}/cancelar")
    @PreAuthorize(QUEM_MONTA_PEDIDO)
    @Operation(summary = "Cancela o pedido (cliente só antes de pagar)")
    public CancelamentoResponse cancelar(@PathVariable Long numero, @AuthenticationPrincipal Usuario usuario) {
        return pedidoService.cancelar(numero, usuario);
    }
}
