import { dinheiro, el, selo } from "../ui.js";

export function comanda(pedido, opcoes) {
    const config = opcoes || {};
    const podeRemover = config.aoRemover && pedido.situacao === "CRIADO";

    const itens = pedido.itens.map((item) => el("li", {},
        el("span", { classe: "qtd" }, item.quantidade + "x"),
        el("span", { classe: "nome" }, item.produto),
        el("span", { classe: "valor" }, dinheiro(item.subtotal)),
        podeRemover
            ? el("button", {
                classe: "remover",
                type: "button",
                "aria-label": "Tirar " + item.produto + " do pedido",
                onclick: () => config.aoRemover(item)
            }, "×")
            : null));

    return el("article", { classe: "comanda", "aria-label": "Pedido número " + pedido.numero },
        el("div", { classe: "comanda-topo" },
            el("span", { classe: "comanda-numero" }, el("small", {}, "nº"), pedido.numero),
            selo(pedido.situacao)),
        config.mostrarCliente ? el("p", { classe: "comanda-cliente" }, pedido.cliente) : null,
        itens.length ? el("ul", { classe: "comanda-itens" }, itens) : el("p", { classe: "discreto" }, "Nenhum item ainda."),
        el("div", { classe: "comanda-total" },
            el("span", {}, "Total"),
            el("span", { classe: "valor" }, dinheiro(pedido.total))),
        config.acoes && config.acoes.length ? el("div", { classe: "comanda-acoes" }, config.acoes) : null);
}
