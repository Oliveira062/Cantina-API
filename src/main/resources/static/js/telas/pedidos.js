import { enviar, obter } from "../api.js";
import { abrirFormulario, avisar, avisarErro, campo, dataHora, dinheiro, el, hora, selo, SITUACOES, vazio } from "../ui.js";

const FUNCIONARIOS_DO_BALCAO = ["ATENDENTE", "ADMIN"];
const FUNCIONARIOS_DA_COZINHA = ["COZINHA", "ADMIN"];

export function telaPedidos(area, usuario) {
    const ehCliente = usuario.perfil === "CLIENTE";
    let filtro = "";
    let aberto = null;
    const filtros = el("div", { classe: "filtros", role: "group", "aria-label": "Filtrar por situação" });
    const lista = el("div");

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, ehCliente ? "Meus pedidos" : "Pedidos"),
                el("p", { classe: "discreto" }, ehCliente
                    ? "Toque em um pedido para ver os itens e cada mudança de situação."
                    : "Toque em um pedido para ver os detalhes e as ações disponíveis para o seu perfil.")),
            el("button", { classe: "botao", type: "button", onclick: () => carregar() }, "Atualizar")),
        filtros,
        el("div", { classe: "espaco" }),
        lista);

    function desenharFiltros() {
        const opcoes = [["", "Todos"]].concat(Object.entries(SITUACOES));
        filtros.replaceChildren(...opcoes.map(([valor, texto]) => el("button", {
            classe: "filtro",
            type: "button",
            "aria-pressed": String(filtro === valor),
            onclick: () => {
                filtro = valor;
                desenharFiltros();
                carregar();
            }
        }, texto)));
    }

    async function carregar() {
        try {
            const pedidos = await obter("/api/pedidos" + (filtro ? "?situacao=" + filtro : ""));
            desenhar(pedidos);
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function desenhar(pedidos) {
        if (!pedidos.length) {
            lista.replaceChildren(vazio(ehCliente
                ? "Você ainda não fez pedidos. Escolha algo no cardápio."
                : "Nenhum pedido nesta situação."));
            return;
        }
        const corpo = el("tbody");
        for (const pedido of pedidos) {
            const linha = el("tr", { classe: "linha-pedido", tabindex: "0", "aria-expanded": String(aberto === pedido.numero) },
                el("td", {}, el("strong", {}, "nº " + pedido.numero)),
                ehCliente ? null : el("td", {}, pedido.cliente),
                el("td", {}, selo(pedido.situacao)),
                el("td", { classe: "valor" }, dinheiro(pedido.total)),
                el("td", { classe: "discreto ocultar-celular" }, dataHora(pedido.criadoEm)));
            const alternar = () => {
                aberto = aberto === pedido.numero ? null : pedido.numero;
                desenhar(pedidos);
            };
            linha.addEventListener("click", alternar);
            linha.addEventListener("keydown", (evento) => {
                if (evento.key === "Enter" || evento.key === " ") {
                    evento.preventDefault();
                    alternar();
                }
            });
            corpo.append(linha);
            if (aberto === pedido.numero) {
                const celula = el("td", { colspan: ehCliente ? 4 : 5 }, el("p", { classe: "discreto" }, "Carregando..."));
                corpo.append(el("tr", { classe: "detalhe-pedido" }, celula));
                desenharDetalhe(pedido, celula);
            }
        }
        lista.replaceChildren(el("table", { classe: "tabela" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Pedido"),
                ehCliente ? null : el("th", {}, "Cliente"),
                el("th", {}, "Situação"),
                el("th", {}, "Total"),
                el("th", { classe: "ocultar-celular" }, "Aberto em"))),
            corpo));
    }

    async function desenharDetalhe(pedido, celula) {
        let historico = [];
        try {
            historico = await obter(`/api/pedidos/${pedido.numero}/historico`);
        } catch (erro) {
            avisarErro(erro);
        }
        celula.replaceChildren(el("div", { classe: "detalhe-grade" },
            el("div", {},
                el("h3", {}, "Itens"),
                el("ul", { classe: "comanda-itens" }, pedido.itens.map((item) => el("li", {},
                    el("span", { classe: "qtd" }, item.quantidade + "x"),
                    el("span", { classe: "nome" }, item.produto),
                    el("span", { classe: "valor" }, dinheiro(item.subtotal))))),
                pedido.pagamento
                    ? el("p", { classe: "discreto" }, `Pago com ${nomePagamento(pedido.pagamento)}` +
                        (Number(pedido.troco) > 0 ? `, troco de ${dinheiro(pedido.troco)}` : ""))
                    : null,
                el("div", { classe: "tabela-acoes" }, acoesDoPedido(pedido))),
            el("div", {},
                el("h3", {}, "Histórico"),
                el("ol", { classe: "linha-do-tempo" }, historico.map((passo) => el("li", {},
                    el("strong", {}, SITUACOES[passo.para]),
                    el("p", { classe: "discreto" }, `${hora(passo.registradoEm)} · ${passo.responsavel}`)))))));
    }

    function acoesDoPedido(pedido) {
        const perfil = usuario.perfil;
        const acoes = [];
        const aguardando = pedido.situacao === "CRIADO" || pedido.situacao === "AGUARDANDO_PAGAMENTO";

        if (aguardando && FUNCIONARIOS_DO_BALCAO.includes(perfil) && pedido.itens.length) {
            acoes.push(botao("Registrar pagamento", "botao-principal", () => registrarPagamento(pedido)));
        }
        if (pedido.situacao === "PAGO" && FUNCIONARIOS_DA_COZINHA.includes(perfil)) {
            acoes.push(botao("Começar preparo", "botao-principal", () => mudar(pedido, "preparo", "Preparo iniciado.")));
        }
        if (pedido.situacao === "EM_PREPARACAO" && FUNCIONARIOS_DA_COZINHA.includes(perfil)) {
            acoes.push(botao("Marcar como pronto", "botao-principal", () => mudar(pedido, "pronto", "Pedido pronto.")));
        }
        if (pedido.situacao === "PRONTO" && FUNCIONARIOS_DO_BALCAO.includes(perfil)) {
            acoes.push(botao("Confirmar entrega", "botao-principal", () => mudar(pedido, "entrega", "Entrega confirmada.")));
        }
        const finalizado = pedido.situacao === "ENTREGUE" || pedido.situacao === "CANCELADO";
        const podeCancelar = perfil === "CLIENTE" ? aguardando : FUNCIONARIOS_DO_BALCAO.includes(perfil) && !finalizado;
        if (podeCancelar) {
            acoes.push(botao("Cancelar pedido", "botao-perigo", () => cancelar(pedido)));
        }
        return acoes;
    }

    function botao(texto, classe, acao) {
        return el("button", { classe: "botao botao-pequeno " + classe, type: "button", onclick: acao }, texto);
    }

    async function mudar(pedido, acao, mensagem) {
        try {
            await enviar(`/api/pedidos/${pedido.numero}/${acao}`);
            avisar(mensagem, "sucesso");
            carregar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    async function cancelar(pedido) {
        if (!confirm(`Cancelar o pedido nº ${pedido.numero}?`)) {
            return;
        }
        try {
            const resposta = await enviar(`/api/pedidos/${pedido.numero}/cancelar`);
            avisar(resposta.estoqueDevolvido ? "Pedido cancelado e estoque devolvido." : "Pedido cancelado.");
            carregar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function registrarPagamento(pedido) {
        abrirFormulario(`Pagamento do pedido nº ${pedido.numero}`,
            [
                campo("tipo", "Forma de pagamento", { opcoes: [["PIX", "PIX"], ["CARTAO", "Cartão"], ["DINHEIRO", "Dinheiro"]] }),
                campo("valorRecebido", `Valor recebido, só para dinheiro (total ${dinheiro(pedido.total)})`,
                    { tipo: "number", passo: "0.01", min: 0 })
            ],
            "Confirmar pagamento",
            async (dados) => {
                const resposta = await enviar(`/api/pedidos/${pedido.numero}/pagamento`, dados);
                avisar(resposta.mensagem, "sucesso");
                carregar();
            });
    }

    desenharFiltros();
    carregar();
}

function nomePagamento(tipo) {
    return { PIX: "PIX", CARTAO: "cartão", DINHEIRO: "dinheiro" }[tipo] || tipo;
}
