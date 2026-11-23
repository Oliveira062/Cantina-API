import { alterar, obter } from "../api.js";
import { avisarErro, dataHora, el, vazio } from "../ui.js";

const INTERVALO = 20000;

export function sinoDeNotificacoes() {
    const contador = el("span", { classe: "sino-contador", hidden: true });
    const botao = el("button", { classe: "botao botao-pequeno sino", type: "button", "aria-label": "Notificações" }, "Avisos", contador);
    let lista = [];
    let painel = null;

    async function atualizar() {
        try {
            lista = await obter("/api/notificacoes");
            const novas = lista.filter((n) => !n.lida).length;
            contador.textContent = novas > 9 ? "9+" : String(novas);
            contador.hidden = novas === 0;
            if (painel) {
                desenharPainel();
            }
        } catch (erro) {
            if (erro.status !== 401) {
                avisarErro(erro);
            }
        }
    }

    function desenharPainel() {
        const itens = lista.map((n) => el("li", {
            classe: "notificacao" + (n.lida ? "" : " nova"),
            tabindex: "0",
            onclick: () => marcar(n)
        }, el("p", {}, n.mensagem), el("p", { classe: "discreto" }, dataHora(n.criadaEm))));
        painel.replaceChildren(
            el("header", {},
                el("h3", {}, "Avisos"),
                el("button", { classe: "botao botao-pequeno", type: "button", onclick: fechar }, "Fechar")),
            itens.length ? el("ul", {}, itens) : el("div", { classe: "espaco-interno" }, vazio("Nenhum aviso por enquanto.")));
    }

    async function marcar(notificacao) {
        if (notificacao.lida) {
            return;
        }
        try {
            await alterar(`/api/notificacoes/${notificacao.id}/lida`);
            atualizar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function abrir() {
        painel = el("aside", { classe: "painel-notificacoes", "aria-label": "Avisos" });
        document.body.append(painel);
        desenharPainel();
        document.addEventListener("keydown", fecharComEsc);
    }

    function fechar() {
        if (painel) {
            painel.remove();
            painel = null;
        }
        document.removeEventListener("keydown", fecharComEsc);
        botao.focus();
    }

    function fecharComEsc(evento) {
        if (evento.key === "Escape") {
            fechar();
        }
    }

    botao.addEventListener("click", () => (painel ? fechar() : abrir()));
    atualizar();
    const relogio = setInterval(atualizar, INTERVALO);

    return { botao, parar: () => { clearInterval(relogio); fechar(); } };
}
