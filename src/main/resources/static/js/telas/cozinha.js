import { enviar, obter } from "../api.js";
import { avisar, avisarErro, el, hora, vazio } from "../ui.js";
import { comanda } from "./comanda.js";

const INTERVALO_ATUALIZACAO = 10000;

export function telaCozinha(area) {
    const paraPreparar = el("div", { classe: "trilho-tickets" });
    const emPreparo = el("div", { classe: "trilho-tickets" });
    const contagemPreparar = el("span", { classe: "contagem" }, "0");
    const contagemPreparo = el("span", { classe: "contagem" }, "0");
    const atualizado = el("p", { classe: "discreto" });

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, "Cozinha"),
                atualizado)),
        el("div", { classe: "trilho" },
            el("section", { classe: "trilho-coluna" },
                el("h3", {}, "Para preparar", contagemPreparar), paraPreparar),
            el("section", { classe: "trilho-coluna" },
                el("h3", {}, "Em preparo", contagemPreparo), emPreparo)));

    async function carregar() {
        try {
            const fila = await obter("/api/cozinha/fila");
            const pagos = fila.filter((p) => p.situacao === "PAGO");
            const preparando = fila.filter((p) => p.situacao === "EM_PREPARACAO");
            contagemPreparar.textContent = pagos.length;
            contagemPreparo.textContent = preparando.length;
            paraPreparar.replaceChildren(...(pagos.length
                ? pagos.map((p) => ticket(p, "Começar preparo", "preparo"))
                : [vazio("Nenhum pedido esperando.")]));
            emPreparo.replaceChildren(...(preparando.length
                ? preparando.map((p) => ticket(p, "Marcar como pronto", "pronto"))
                : [vazio("Nada no fogo agora.")]));
            atualizado.textContent = "Atualiza sozinha a cada 10 segundos. Última leitura às " + hora(new Date()) + ".";
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function ticket(pedido, texto, acao) {
        return comanda(pedido, {
            mostrarCliente: true,
            acoes: [
                el("p", { classe: "discreto" }, "Pago às " + hora(pedido.atualizadoEm)),
                el("button", {
                    classe: "botao botao-principal",
                    type: "button",
                    onclick: async (evento) => {
                        evento.target.disabled = true;
                        try {
                            await enviar(`/api/pedidos/${pedido.numero}/${acao}`);
                            avisar(acao === "pronto" ? `Pedido nº ${pedido.numero} pronto.` : `Preparo do nº ${pedido.numero} iniciado.`, "sucesso");
                            carregar();
                        } catch (erro) {
                            evento.target.disabled = false;
                            avisarErro(erro);
                        }
                    }
                }, texto)
            ]
        });
    }

    carregar();
    const relogio = setInterval(carregar, INTERVALO_ATUALIZACAO);
    return () => clearInterval(relogio);
}
