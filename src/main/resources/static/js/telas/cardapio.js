import { enviar, obter, remover } from "../api.js";
import { abrirFormulario, avisar, avisarErro, campo, dinheiro, el, vazio } from "../ui.js";
import { comanda } from "./comanda.js";

export async function telaCardapio(area, usuario, cliente) {
    const ehBalcao = Boolean(cliente);
    const estado = { produtos: [], pedido: null };
    const listaArea = el("div");
    const comandaArea = el("div", { classe: "coluna-fixa" });

    const colunas = el("div", { classe: "duas-colunas" }, listaArea, comandaArea);
    if (ehBalcao) {
        area.replaceChildren(colunas);
    } else {
        area.replaceChildren(
            el("div", { classe: "cabecalho-tela" },
                el("div", {},
                    el("h2", {}, "Cardápio"),
                    el("p", { classe: "discreto" }, "Escolha a quantidade e adicione. Você paga no fim e acompanha o preparo em Meus pedidos."))),
            colunas);
    }

    async function carregar() {
        try {
            const [produtos, criados, aguardando] = await Promise.all([
                obter("/api/produtos"),
                obter("/api/pedidos?situacao=CRIADO"),
                obter("/api/pedidos?situacao=AGUARDANDO_PAGAMENTO")
            ]);
            estado.produtos = produtos;
            const abertos = criados.concat(aguardando).filter((p) => !ehBalcao || p.clienteId === cliente.id);
            estado.pedido = abertos.length ? abertos[0] : null;
            desenharLista();
            desenharComanda();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function desenharLista() {
        if (!estado.produtos.length) {
            listaArea.replaceChildren(vazio("O cardápio está vazio no momento."));
            return;
        }
        listaArea.replaceChildren(el("div", { classe: "lista-cardapio" }, estado.produtos.map(linhaProduto)));
    }

    function linhaProduto(produto) {
        const quantidade = el("input", {
            type: "number", min: 1, max: Math.max(1, Math.min(50, produto.quantidadeEstoque)), value: 1,
            "aria-label": "Quantidade de " + produto.nome
        });
        const podeComprar = produto.disponivel && (!estado.pedido || estado.pedido.situacao === "CRIADO");
        return el("div", { classe: "item-cardapio" + (produto.disponivel ? "" : " esgotado") },
            el("div", {},
                el("h3", {}, produto.nome),
                el("p", { classe: "discreto" },
                    produto.descricao,
                    produto.disponivel ? ` · ${produto.quantidadeEstoque} disponíveis` : " · Esgotado")),
            el("span", { classe: "preco valor" }, dinheiro(produto.preco)),
            produto.disponivel
                ? el("div", { classe: "quantidade" },
                    quantidade,
                    el("button", {
                        classe: "botao botao-pequeno",
                        type: "button",
                        disabled: !podeComprar,
                        onclick: () => adicionar(produto, Number(quantidade.value))
                    }, "Adicionar"))
                : el("span", { classe: "discreto" }, "Esgotado"));
    }

    async function adicionar(produto, quantidade) {
        try {
            if (!estado.pedido) {
                estado.pedido = await enviar("/api/pedidos", ehBalcao ? { clienteId: cliente.id } : undefined);
            }
            estado.pedido = await enviar(`/api/pedidos/${estado.pedido.numero}/itens`,
                { codigoProduto: produto.codigo, quantidade });
            avisar(`${quantidade}x ${produto.nome} no pedido.`);
            desenharComanda();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    async function tirar(item) {
        try {
            estado.pedido = await remover(`/api/pedidos/${estado.pedido.numero}/itens/${item.codigoProduto}`);
            desenharComanda();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    async function pagar(tipo, valorRecebido) {
        const resposta = await enviar(`/api/pedidos/${estado.pedido.numero}/pagamento`, { tipo, valorRecebido });
        avisar(resposta.mensagem, "sucesso");
        const pago = resposta.pedido;
        estado.pedido = null;
        await carregar();
        comandaArea.replaceChildren(
            comanda(pago, { mostrarCliente: ehBalcao }),
            el("p", { classe: "discreto observacao" },
                ehBalcao
                    ? "Pagamento registrado. O pedido já está na fila da cozinha."
                    : "Pagamento aprovado. Você recebe um aviso quando ficar pronto."));
    }

    function receberEmDinheiro() {
        const total = Number(estado.pedido.total);
        const troco = el("p", { classe: "troco" }, "Troco: " + dinheiro(0));
        const formulario = abrirFormulario("Receber em dinheiro",
            [campo("valorRecebido", `Valor recebido (total ${dinheiro(total)})`, { tipo: "number", passo: "0.01", min: total, obrigatorio: true })],
            "Confirmar pagamento",
            (dados) => pagar("DINHEIRO", dados.valorRecebido),
            troco);
        formulario.querySelector("input").addEventListener("input", (evento) => {
            const recebido = Number(evento.target.value || 0);
            troco.textContent = recebido >= total ? "Troco: " + dinheiro(recebido - total) : "Faltam " + dinheiro(total - recebido);
        });
    }

    async function cancelar() {
        if (!confirm(`Cancelar o pedido nº ${estado.pedido.numero}?`)) {
            return;
        }
        try {
            await enviar(`/api/pedidos/${estado.pedido.numero}/cancelar`);
            avisar("Pedido cancelado.");
            estado.pedido = null;
            carregar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function desenharComanda() {
        desenharLista();
        comandaArea.classList.toggle("tem-pedido", Boolean(estado.pedido));
        if (!estado.pedido) {
            comandaArea.replaceChildren(el("article", { classe: "comanda" },
                el("div", { classe: "comanda-vazia" },
                    el("h3", {}, "Seu pedido aparece aqui"),
                    el("p", { classe: "discreto" }, "Adicione um item do cardápio para começar."))));
            return;
        }
        const temItens = estado.pedido.itens.length > 0;
        const pagarComTratamento = (tipo) => pagar(tipo).catch(avisarErro);
        const acoes = [
            el("div", { classe: "linha" },
                el("button", { classe: "botao botao-principal", type: "button", disabled: !temItens, onclick: () => pagarComTratamento("PIX") }, "Pagar com PIX"),
                el("button", { classe: "botao botao-principal", type: "button", disabled: !temItens, onclick: () => pagarComTratamento("CARTAO") }, "Pagar no cartão")),
            ehBalcao || usuario.perfil === "ADMIN"
                ? el("button", { classe: "botao", type: "button", disabled: !temItens, onclick: receberEmDinheiro }, "Receber em dinheiro")
                : null,
            el("button", { classe: "botao botao-perigo", type: "button", onclick: cancelar }, "Cancelar pedido")
        ].filter(Boolean);
        comandaArea.replaceChildren(comanda(estado.pedido, { aoRemover: tirar, acoes, mostrarCliente: ehBalcao }));
    }

    await carregar();
}

export function telaBalcao(area, usuario) {
    const resultado = el("div", { classe: "resultado-busca" });
    const busca = el("input", { type: "search", placeholder: "Nome, e-mail ou matrícula", "aria-label": "Buscar cliente" });
    const pedidoArea = el("div");

    async function buscar(evento) {
        evento.preventDefault();
        try {
            const clientes = await obter("/api/clientes?busca=" + encodeURIComponent(busca.value.trim()));
            resultado.replaceChildren(...(clientes.length
                ? clientes.map((cliente) => el("button", { type: "button", onclick: () => escolher(cliente) },
                    el("strong", {}, cliente.nome),
                    el("span", { classe: "discreto" }, "  " + cliente.email + (cliente.matricula ? " · matrícula " + cliente.matricula : ""))))
                : [vazio("Nenhum cliente encontrado. Peça para a pessoa criar a conta pelo celular.")]));
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function escolher(cliente) {
        resultado.replaceChildren();
        formulario.hidden = true;
        pedidoArea.replaceChildren(
            el("div", { classe: "cliente-escolhido" },
                el("span", {}, "Pedido para ", el("strong", {}, cliente.nome)),
                el("button", { classe: "botao botao-pequeno", type: "button", onclick: trocar }, "Trocar cliente")),
            el("div"));
        telaCardapio(pedidoArea.lastChild, usuario, cliente);
    }

    function trocar() {
        pedidoArea.replaceChildren();
        formulario.hidden = false;
        busca.value = "";
        busca.focus();
    }

    const formulario = el("form", { classe: "busca-cliente", onsubmit: buscar },
        busca,
        el("button", { classe: "botao", type: "submit" }, "Buscar"));

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, "Balcão"),
                el("p", { classe: "discreto" }, "Encontre o cliente, monte o pedido e registre o pagamento."))),
        formulario,
        resultado,
        pedidoArea);
    busca.focus();
}
