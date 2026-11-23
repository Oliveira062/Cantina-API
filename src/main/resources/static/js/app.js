import { apagarToken, obter, quandoPerderSessao, tokenSalvo } from "./api.js";
import { el, PERFIS } from "./ui.js";
import { telaEntrada } from "./telas/entrada.js";
import { telaBalcao, telaCardapio } from "./telas/cardapio.js";
import { telaPedidos } from "./telas/pedidos.js";
import { telaCozinha } from "./telas/cozinha.js";
import { telaCantina, telaEquipe, telaPainel, telaProdutos } from "./telas/admin.js";
import { sinoDeNotificacoes } from "./telas/notificacoes.js";

const raiz = document.getElementById("app");

const ABAS = {
    cardapio: { titulo: "Cardápio", abrir: (area, usuario) => telaCardapio(area, usuario) },
    "meus-pedidos": { titulo: "Meus pedidos", abrir: telaPedidos },
    balcao: { titulo: "Balcão", abrir: telaBalcao },
    pedidos: { titulo: "Pedidos", abrir: telaPedidos },
    cozinha: { titulo: "Cozinha", abrir: telaCozinha },
    painel: { titulo: "Painel", abrir: telaPainel },
    produtos: { titulo: "Produtos", abrir: telaProdutos },
    equipe: { titulo: "Equipe", abrir: telaEquipe },
    cantina: { titulo: "Cantina", abrir: (area) => telaCantina(area, atualizarCantina) }
};

const ABAS_POR_PERFIL = {
    CLIENTE: ["cardapio", "meus-pedidos"],
    ATENDENTE: ["balcao", "pedidos"],
    COZINHA: ["cozinha", "pedidos"],
    ADMIN: ["painel", "pedidos", "cozinha", "balcao", "produtos", "equipe", "cantina"]
};

let cantina = null;
let limparTela = null;
let sino = null;
let seloCantina = null;

async function iniciar() {
    quandoPerderSessao(() => sair("Sua sessão terminou. Entre de novo para continuar."));
    try {
        cantina = await obter("/api/cantina");
    } catch (erro) {
        cantina = null;
    }
    if (!tokenSalvo()) {
        mostrarEntrada();
        return;
    }
    try {
        montarSistema(await obter("/api/auth/eu"));
    } catch (erro) {
        mostrarEntrada();
    }
}

function mostrarEntrada(aviso) {
    telaEntrada(raiz, cantina, montarSistema, aviso);
}

function sair(aviso) {
    apagarToken();
    if (limparTela) {
        limparTela();
        limparTela = null;
    }
    if (sino) {
        sino.parar();
        sino = null;
    }
    history.replaceState(null, "", "/");
    mostrarEntrada(aviso);
}

function atualizarCantina(nova) {
    cantina = nova;
    if (seloCantina) {
        seloCantina.replaceWith(criarSeloCantina());
    }
}

function criarSeloCantina() {
    seloCantina = cantina
        ? el("span", { classe: "situacao-cantina " + (cantina.abertaAgora ? "aberta" : "fechada") },
            cantina.abertaAgora ? "Aberta agora" : "Fechada agora")
        : el("span");
    return seloCantina;
}

function montarSistema(usuario) {
    const abas = ABAS_POR_PERFIL[usuario.perfil] || [];
    const area = el("main", { classe: "conteudo", id: "conteudo", tabindex: "-1" });
    const botoesAbas = abas.map((chave) => el("button", {
        classe: "aba",
        type: "button",
        role: "tab",
        "data-aba": chave,
        onclick: () => abrirAba(chave)
    }, ABAS[chave].titulo));

    sino = sinoDeNotificacoes();

    raiz.replaceChildren(
        el("header", { classe: "topo" },
            el("div", { classe: "topo-linha" },
                el("span", { classe: "topo-nome" }, cantina ? cantina.nome : "Cantina"),
                criarSeloCantina(),
                el("div", { classe: "topo-usuario" },
                    el("span", { classe: "nome-usuario" }, `${usuario.nome} · ${PERFIS[usuario.perfil]}`),
                    sino.botao,
                    el("button", { classe: "botao botao-pequeno", type: "button", onclick: () => sair() }, "Sair"))),
            el("nav", { classe: "abas", role: "tablist", "aria-label": "Seções" }, botoesAbas)),
        area);

    function abrirAba(chave) {
        if (!abas.includes(chave)) {
            chave = abas[0];
        }
        if (limparTela) {
            limparTela();
            limparTela = null;
        }
        for (const botao of botoesAbas) {
            botao.setAttribute("aria-selected", String(botao.dataset.aba === chave));
        }
        history.replaceState(null, "", "#" + chave);
        const resultado = ABAS[chave].abrir(area, usuario);
        if (typeof resultado === "function") {
            limparTela = resultado;
        }
    }

    abrirAba(location.hash.substring(1));
}

iniciar();
