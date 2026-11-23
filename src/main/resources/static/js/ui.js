const formatoDinheiro = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const formatoHora = new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" });
const formatoDataHora = new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit"
});

export const SITUACOES = {
    CRIADO: "Criado",
    AGUARDANDO_PAGAMENTO: "Aguardando pagamento",
    PAGO: "Pago",
    EM_PREPARACAO: "Em preparação",
    PRONTO: "Pronto",
    ENTREGUE: "Entregue",
    CANCELADO: "Cancelado"
};

export const PERFIS = {
    CLIENTE: "Cliente",
    ATENDENTE: "Atendente",
    COZINHA: "Cozinha",
    ADMIN: "Administrador"
};

export function el(tag, atributos, ...filhos) {
    const elemento = document.createElement(tag);
    for (const [nome, valor] of Object.entries(atributos || {})) {
        if (valor === undefined || valor === null || valor === false) {
            continue;
        }
        if (nome.startsWith("on") && typeof valor === "function") {
            elemento.addEventListener(nome.substring(2), valor);
        } else if (nome === "classe") {
            elemento.className = valor;
        } else if (valor === true) {
            elemento.setAttribute(nome, "");
        } else {
            elemento.setAttribute(nome, valor);
        }
    }
    for (const filho of filhos.flat()) {
        if (filho === undefined || filho === null || filho === false) {
            continue;
        }
        elemento.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
    }
    return elemento;
}

export function dinheiro(valor) {
    return formatoDinheiro.format(Number(valor || 0));
}

export function hora(instante) {
    return instante ? formatoHora.format(new Date(instante)) : "";
}

export function dataHora(instante) {
    return instante ? formatoDataHora.format(new Date(instante)) : "";
}

export function selo(situacao) {
    return el("span", { classe: "selo selo-" + situacao }, SITUACOES[situacao] || situacao);
}

export function avisar(mensagem, tipo) {
    const aviso = el("div", { classe: "aviso" + (tipo ? " aviso-" + tipo : ""), role: "status" }, mensagem);
    document.getElementById("avisos").append(aviso);
    setTimeout(() => aviso.remove(), tipo === "erro" ? 6000 : 3500);
}

export function avisarErro(erro) {
    avisar(erro.message || "Algo deu errado.", "erro");
}

export function vazio(texto) {
    return el("div", { classe: "vazio" }, texto);
}

export function campo(nome, rotulo, opcoes) {
    const config = opcoes || {};
    const id = "campo-" + nome;
    let entrada;
    if (config.opcoes) {
        entrada = el("select", { id, name: nome },
            config.opcoes.map(([valor, texto]) => el("option", { value: valor, selected: valor === config.valor }, texto)));
    } else {
        entrada = el("input", {
            id,
            name: nome,
            type: config.tipo || "text",
            value: config.valor,
            required: config.obrigatorio,
            min: config.min,
            max: config.max,
            step: config.passo,
            autocomplete: config.autocompletar,
            placeholder: config.exemplo
        });
    }
    if (config.tipo === "checkbox") {
        entrada.checked = Boolean(config.valor);
        return el("div", { classe: "campo campo-marcador" }, entrada, el("label", { for: id }, rotulo));
    }
    return el("div", { classe: "campo" },
        el("label", { for: id }, rotulo),
        entrada,
        el("span", { classe: "erro-campo", "data-erro": nome }));
}

export function lerFormulario(formulario) {
    const dados = {};
    for (const entrada of formulario.querySelectorAll("input, select")) {
        if (!entrada.name) {
            continue;
        }
        if (entrada.type === "checkbox") {
            dados[entrada.name] = entrada.checked;
        } else if (entrada.type === "number") {
            dados[entrada.name] = entrada.value === "" ? null : Number(entrada.value);
        } else {
            dados[entrada.name] = entrada.value.trim();
        }
    }
    return dados;
}

export function mostrarErrosDosCampos(formulario, erro) {
    for (const marcador of formulario.querySelectorAll("[data-erro]")) {
        marcador.textContent = "";
    }
    const campos = erro.campos || {};
    let algumCampo = false;
    for (const [nome, mensagem] of Object.entries(campos)) {
        const marcador = formulario.querySelector(`[data-erro="${nome}"]`);
        if (marcador) {
            marcador.textContent = mensagem;
            algumCampo = true;
        }
    }
    if (!algumCampo || Object.keys(campos).length === 0) {
        avisarErro(erro);
    }
}

export function abrirFormulario(titulo, campos, textoBotao, aoEnviar, extra) {
    const dialogo = document.getElementById("dialogo");
    const botaoEnviar = el("button", { classe: "botao botao-principal", type: "submit" }, textoBotao);
    const formulario = el("form", { method: "dialog" },
        el("h3", {}, titulo),
        campos,
        extra,
        el("div", { classe: "dialogo-acoes" },
            el("button", { classe: "botao", type: "button", onclick: () => dialogo.close() }, "Voltar"),
            botaoEnviar));

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        botaoEnviar.disabled = true;
        try {
            await aoEnviar(lerFormulario(formulario), formulario);
            dialogo.close();
        } catch (erro) {
            mostrarErrosDosCampos(formulario, erro);
        } finally {
            botaoEnviar.disabled = false;
        }
    });

    dialogo.replaceChildren(formulario);
    dialogo.showModal();
    const primeiro = formulario.querySelector("input, select");
    if (primeiro) {
        primeiro.focus();
    }
    return formulario;
}
