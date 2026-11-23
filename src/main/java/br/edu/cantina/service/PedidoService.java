package br.edu.cantina.service;

import br.edu.cantina.dto.CancelamentoResponse;
import br.edu.cantina.dto.HistoricoResponse;
import br.edu.cantina.dto.PagamentoResponse;
import br.edu.cantina.dto.PedidoResponse;
import br.edu.cantina.exception.DadosInvalidosException;
import br.edu.cantina.exception.OperacaoPedidoInvalidaException;
import br.edu.cantina.exception.PagamentoInvalidoException;
import br.edu.cantina.exception.PedidoNaoEncontradoException;
import br.edu.cantina.model.HistoricoPedido;
import br.edu.cantina.model.Pedido;
import br.edu.cantina.model.Produto;
import br.edu.cantina.model.SituacaoPedido;
import br.edu.cantina.model.Usuario;
import br.edu.cantina.observer.PedidoAlteradoEvento;
import br.edu.cantina.pagamento.EstrategiaPagamento;
import br.edu.cantina.pagamento.PagamentoFactory;
import br.edu.cantina.pagamento.ResultadoPagamento;
import br.edu.cantina.pagamento.TipoPagamento;
import br.edu.cantina.repository.HistoricoPedidoRepository;
import br.edu.cantina.repository.PedidoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final List<SituacaoPedido> FILA_DA_COZINHA =
            List.of(SituacaoPedido.PAGO, SituacaoPedido.EM_PREPARACAO);

    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoRepository historicoRepository;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final ConfiguracaoService configuracaoService;
    private final PagamentoFactory pagamentoFactory;
    private final ApplicationEventPublisher publicador;

    public PedidoService(PedidoRepository pedidoRepository, HistoricoPedidoRepository historicoRepository,
                         ProdutoService produtoService, UsuarioService usuarioService,
                         ConfiguracaoService configuracaoService, PagamentoFactory pagamentoFactory,
                         ApplicationEventPublisher publicador) {
        this.pedidoRepository = pedidoRepository;
        this.historicoRepository = historicoRepository;
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
        this.configuracaoService = configuracaoService;
        this.pagamentoFactory = pagamentoFactory;
        this.publicador = publicador;
    }

    @Transactional
    public PedidoResponse criar(Long clienteId, Usuario solicitante) {
        configuracaoService.verificarFuncionamento();
        Usuario cliente = definirCliente(clienteId, solicitante);
        Pedido pedido = pedidoRepository.save(new Pedido(cliente));
        historicoRepository.save(new HistoricoPedido(pedido.getId(), null, SituacaoPedido.CRIADO, solicitante));
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse adicionarItem(Long numero, Long codigoProduto, int quantidade, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        Produto produto = produtoService.buscar(codigoProduto);
        pedido.adicionarItem(produto, quantidade);
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse removerItem(Long numero, Long codigoProduto, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        pedido.removerItem(codigoProduto);
        return responder(pedido);
    }

    @Transactional
    public PedidoResponse finalizar(Long numero, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        SituacaoPedido anterior = pedido.getSituacao();
        pedido.finalizar();
        registrarMudanca(pedido, anterior, usuario);
        return responder(pedido);
    }

    @Transactional
    public PagamentoResponse pagar(Long numero, String tipo, BigDecimal valorRecebido, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        TipoPagamento tipoPagamento = TipoPagamento.de(tipo);
        if (tipoPagamento == TipoPagamento.DINHEIRO && usuario.isCliente()) {
            throw new PagamentoInvalidoException("Pagamento em dinheiro só pode ser registrado no balcão, pelo atendente.");
        }
        if (pedido.getSituacao() == SituacaoPedido.CRIADO) {
            finalizarSemResponder(pedido, usuario);
        }

        SituacaoPedido anterior = pedido.getSituacao();
        EstrategiaPagamento estrategia = pagamentoFactory.criar(tipoPagamento);
        ResultadoPagamento resultado = pedido.registrarPagamento(estrategia, valorRecebido,
                configuracaoService.taxaServico());
        if (resultado.aprovado()) {
            registrarMudanca(pedido, anterior, usuario);
        }
        return new PagamentoResponse(resultado.aprovado(), resultado.mensagem(), resultado.troco(), responder(pedido));
    }

    @Transactional
    public PedidoResponse iniciarPreparo(Long numero, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        if (pedido.getSituacao() != SituacaoPedido.PAGO) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "Somente pedidos pagos podem entrar em preparo. O pedido nº %d está \"%s\".",
                    numero, pedido.getSituacao().getDescricao()));
        }
        return mudarSituacao(pedido, SituacaoPedido.EM_PREPARACAO, usuario);
    }

    @Transactional
    public PedidoResponse marcarPronto(Long numero, Usuario usuario) {
        return mudarSituacao(buscarComAcesso(numero, usuario), SituacaoPedido.PRONTO, usuario);
    }

    @Transactional
    public PedidoResponse confirmarEntrega(Long numero, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        if (pedido.getSituacao() != SituacaoPedido.PRONTO) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "Somente pedidos prontos podem ser entregues. O pedido nº %d está \"%s\".",
                    numero, pedido.getSituacao().getDescricao()));
        }
        return mudarSituacao(pedido, SituacaoPedido.ENTREGUE, usuario);
    }

    @Transactional
    public CancelamentoResponse cancelar(Long numero, Usuario usuario) {
        Pedido pedido = buscarComAcesso(numero, usuario);
        SituacaoPedido anterior = pedido.getSituacao();
        boolean clientePodeCancelar = anterior == SituacaoPedido.CRIADO
                || anterior == SituacaoPedido.AGUARDANDO_PAGAMENTO;
        if (usuario.isCliente() && !clientePodeCancelar) {
            throw new OperacaoPedidoInvalidaException(
                    "Depois de pago, o cancelamento precisa ser feito pelo atendente no balcão.");
        }
        boolean devolvido = pedido.cancelar();
        registrarMudanca(pedido, anterior, usuario);
        return new CancelamentoResponse(devolvido, responder(pedido));
    }

    @Transactional(readOnly = true)
    public PedidoResponse consultar(Long numero, Usuario usuario) {
        return responder(buscarComAcesso(numero, usuario));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(SituacaoPedido situacao, Usuario usuario) {
        List<Pedido> pedidos;
        if (usuario.isCliente()) {
            pedidos = situacao == null
                    ? pedidoRepository.findByClienteIdOrderByIdDesc(usuario.getId())
                    : pedidoRepository.findByClienteIdAndSituacaoOrderByIdDesc(usuario.getId(), situacao);
        } else {
            pedidos = situacao == null
                    ? pedidoRepository.findAllByOrderByIdDesc()
                    : pedidoRepository.findBySituacaoOrderByIdDesc(situacao);
        }
        BigDecimal taxa = configuracaoService.taxaServico();
        return pedidos.stream().map(p -> PedidoResponse.de(p, taxa)).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> filaDaCozinha() {
        BigDecimal taxa = configuracaoService.taxaServico();
        return pedidoRepository.findBySituacaoInOrderByIdAsc(FILA_DA_COZINHA).stream()
                .map(p -> PedidoResponse.de(p, taxa)).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> historico(Long numero, Usuario usuario) {
        buscarComAcesso(numero, usuario);
        return historicoRepository.findByPedidoIdOrderByIdAsc(numero).stream().map(HistoricoResponse::de).toList();
    }

    private Usuario definirCliente(Long clienteId, Usuario solicitante) {
        if (solicitante.isCliente()) {
            if (clienteId != null && !clienteId.equals(solicitante.getId())) {
                throw new DadosInvalidosException("Clientes só podem abrir pedidos para si mesmos.");
            }
            return solicitante;
        }
        if (clienteId == null) {
            throw new DadosInvalidosException("Informe o clienteId para abrir um pedido no balcão.");
        }
        Usuario cliente = usuarioService.buscar(clienteId);
        if (!cliente.isCliente() || !cliente.isAtivo()) {
            throw new DadosInvalidosException("O código informado não pertence a um cliente ativo.");
        }
        return cliente;
    }

    private Pedido buscarComAcesso(Long numero, Usuario usuario) {
        Pedido pedido = pedidoRepository.findById(numero).orElseThrow(() -> new PedidoNaoEncontradoException(numero));
        if (usuario.isCliente() && !pedido.pertenceA(usuario)) {
            throw new PedidoNaoEncontradoException(numero);
        }
        return pedido;
    }

    private void finalizarSemResponder(Pedido pedido, Usuario usuario) {
        SituacaoPedido anterior = pedido.getSituacao();
        pedido.finalizar();
        registrarMudanca(pedido, anterior, usuario);
    }

    private PedidoResponse mudarSituacao(Pedido pedido, SituacaoPedido nova, Usuario usuario) {
        SituacaoPedido anterior = pedido.getSituacao();
        pedido.alterarSituacao(nova);
        registrarMudanca(pedido, anterior, usuario);
        return responder(pedido);
    }

    private void registrarMudanca(Pedido pedido, SituacaoPedido anterior, Usuario usuario) {
        historicoRepository.save(new HistoricoPedido(pedido.getId(), anterior, pedido.getSituacao(), usuario));
        String resumo = pedido.getItens().stream()
                .map(item -> item.getQuantidade() + "x " + item.getProduto().getNome())
                .collect(Collectors.joining(", "));
        publicador.publishEvent(new PedidoAlteradoEvento(pedido.getId(), pedido.getCliente().getId(),
                pedido.getCliente().getNome(), pedido.getSituacao(), resumo));
    }

    private PedidoResponse responder(Pedido pedido) {
        return PedidoResponse.de(pedido, configuracaoService.taxaServico());
    }
}
