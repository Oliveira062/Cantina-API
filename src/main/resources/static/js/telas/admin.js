import { alterar, enviar, obter, remover, substituir } from "../api.js";
import { abrirFormulario, avisar, avisarErro, campo, dinheiro, el, lerFormulario, mostrarErrosDosCampos, PERFIS, SITUACOES, vazio } from "../ui.js";

const ESTOQUE_BAIXO = 5;

export async function telaPainel(area) {
    area.replaceChildren(el("p", { classe: "discreto" }, "Carregando..."));
    try {
        const [pedidos, produtos] = await Promise.all([obter("/api/pedidos"), obter("/api/produtos")]);
        const hoje = new Date().toDateString();
        const deHoje = pedidos.filter((p) => new Date(p.criadoEm).toDateString() === hoje);
        const faturamento = deHoje
            .filter((p) => p.pagamento && p.situacao !== "CANCELADO")
            .reduce((soma, p) => soma + Number(p.total), 0);
        const acabando = produtos.filter((p) => p.ativo && p.quantidadeEstoque <= ESTOQUE_BAIXO);

        const contadores = Object.entries(SITUACOES).map(([situacao, nome]) => el("div", { classe: "contador" },
            el("strong", {}, pedidos.filter((p) => p.situacao === situacao).length),
            el("span", {}, nome)));

        area.replaceChildren(
            el("div", { classe: "cabecalho-tela" },
                el("div", {},
                    el("h2", {}, "Painel"),
                    el("p", { classe: "discreto" }, "Visão geral dos pedidos e do estoque."))),
            el("div", { classe: "contadores" },
                el("div", { classe: "contador destaque" },
                    el("strong", { classe: "valor" }, dinheiro(faturamento)),
                    el("span", {}, `Recebido hoje em ${deHoje.length} pedido(s)`)),
                contadores),
            el("section", { classe: "bloco" },
                el("h3", {}, "Estoque baixo"),
                acabando.length
                    ? el("table", { classe: "tabela" },
                        el("tbody", {}, acabando.map((p) => el("tr", {},
                            el("td", {}, p.nome),
                            el("td", { classe: "valor" }, p.quantidadeEstoque === 0 ? "Esgotado" : p.quantidadeEstoque + " restantes")))))
                    : vazio("Todos os produtos têm mais de " + ESTOQUE_BAIXO + " unidades.")));
    } catch (erro) {
        avisarErro(erro);
    }
}

export async function telaProdutos(area) {
    const tabela = el("div");

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, "Produtos"),
                el("p", { classe: "discreto" }, "Produtos desativados saem do cardápio, mas continuam nos pedidos antigos.")),
            el("button", { classe: "botao botao-principal", type: "button", onclick: novoProduto }, "Cadastrar produto")),
        tabela);

    async function carregar() {
        try {
            desenhar(await obter("/api/produtos"));
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function desenhar(produtos) {
        tabela.replaceChildren(el("table", { classe: "tabela" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Produto"),
                el("th", {}, "Preço"),
                el("th", {}, "Estoque"),
                el("th", { classe: "ocultar-celular" }, "Situação"),
                el("th", {}))),
            el("tbody", {}, produtos.map((p) => el("tr", {},
                el("td", {}, el("strong", {}, p.nome), el("div", { classe: "discreto" }, p.descricao)),
                el("td", { classe: "valor" }, dinheiro(p.preco)),
                el("td", { classe: "valor" }, p.quantidadeEstoque),
                el("td", { classe: "ocultar-celular" }, p.ativo ? "No cardápio" : "Desativado"),
                el("td", {}, el("div", { classe: "acoes" },
                    el("button", { classe: "botao botao-pequeno", type: "button", onclick: () => mudarPreco(p) }, "Preço"),
                    el("button", { classe: "botao botao-pequeno", type: "button", onclick: () => reporEstoque(p) }, "Repor"),
                    p.ativo
                        ? el("button", { classe: "botao botao-pequeno botao-perigo", type: "button", onclick: () => ativar(p, false) }, "Desativar")
                        : el("button", { classe: "botao botao-pequeno", type: "button", onclick: () => ativar(p, true) }, "Reativar"))))))));
    }

    function novoProduto() {
        abrirFormulario("Cadastrar produto", [
            campo("nome", "Nome", { obrigatorio: true }),
            campo("descricao", "Descrição"),
            campo("preco", "Preço (R$)", { tipo: "number", passo: "0.01", min: "0.01", obrigatorio: true }),
            campo("quantidadeInicial", "Quantidade inicial", { tipo: "number", min: 0, valor: 0, obrigatorio: true })
        ], "Cadastrar", async (dados) => {
            await enviar("/api/produtos", dados);
            avisar("Produto cadastrado.", "sucesso");
            carregar();
        });
    }

    function mudarPreco(produto) {
        abrirFormulario("Preço de " + produto.nome, [
            campo("preco", "Novo preço (R$)", { tipo: "number", passo: "0.01", min: "0.01", valor: produto.preco, obrigatorio: true })
        ], "Salvar preço", async (dados) => {
            await alterar(`/api/produtos/${produto.codigo}/preco`, dados);
            avisar("Preço salvo.", "sucesso");
            carregar();
        });
    }

    function reporEstoque(produto) {
        abrirFormulario("Repor " + produto.nome, [
            campo("quantidade", `Quantas unidades chegaram? (hoje: ${produto.quantidadeEstoque})`, { tipo: "number", min: 1, obrigatorio: true })
        ], "Repor estoque", async (dados) => {
            await enviar(`/api/produtos/${produto.codigo}/estoque`, dados);
            avisar("Estoque atualizado.", "sucesso");
            carregar();
        });
    }

    async function ativar(produto, ativo) {
        try {
            if (ativo) {
                await enviar(`/api/produtos/${produto.codigo}/reativar`);
            } else {
                await remover(`/api/produtos/${produto.codigo}`);
            }
            avisar(ativo ? "Produto de volta ao cardápio." : "Produto desativado.");
            carregar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    carregar();
}

export async function telaEquipe(area, usuario) {
    const tabela = el("div");

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, "Equipe e contas"),
                el("p", { classe: "discreto" }, "Clientes criam a própria conta. Funcionários são cadastrados aqui.")),
            el("button", { classe: "botao botao-principal", type: "button", onclick: novoFuncionario }, "Cadastrar funcionário")),
        tabela);

    async function carregar() {
        try {
            desenhar(await obter("/api/usuarios"));
        } catch (erro) {
            avisarErro(erro);
        }
    }

    function desenhar(usuarios) {
        tabela.replaceChildren(el("table", { classe: "tabela" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Nome"),
                el("th", { classe: "ocultar-celular" }, "E-mail"),
                el("th", {}, "Perfil"),
                el("th", {}))),
            el("tbody", {}, usuarios.map((u) => el("tr", {},
                el("td", {}, el("strong", {}, u.nome), u.ativo ? null : el("div", { classe: "discreto" }, "Conta desativada")),
                el("td", { classe: "ocultar-celular" }, u.email),
                el("td", {}, PERFIS[u.perfil]),
                el("td", {}, u.id === usuario.id ? el("span", { classe: "discreto" }, "Você") : el("div", { classe: "acoes" },
                    el("button", {
                        classe: "botao botao-pequeno" + (u.ativo ? " botao-perigo" : ""),
                        type: "button",
                        onclick: () => mudarStatus(u)
                    }, u.ativo ? "Desativar" : "Reativar"))))))));
    }

    function novoFuncionario() {
        abrirFormulario("Cadastrar funcionário", [
            campo("nome", "Nome", { obrigatorio: true }),
            campo("email", "E-mail", { tipo: "email", obrigatorio: true }),
            campo("perfil", "Função", { opcoes: [["ATENDENTE", "Atendente"], ["COZINHA", "Cozinha"], ["ADMIN", "Administrador"]] }),
            campo("senha", "Senha inicial", { tipo: "password", autocompletar: "new-password", obrigatorio: true })
        ], "Cadastrar", async (dados) => {
            await enviar("/api/usuarios", dados);
            avisar("Funcionário cadastrado.", "sucesso");
            carregar();
        });
    }

    async function mudarStatus(alvo) {
        if (alvo.ativo && !confirm(`Desativar a conta de ${alvo.nome}? A pessoa perde o acesso na hora.`)) {
            return;
        }
        try {
            await alterar(`/api/usuarios/${alvo.id}/status`, { ativo: !alvo.ativo });
            avisar(alvo.ativo ? "Conta desativada." : "Conta reativada.");
            carregar();
        } catch (erro) {
            avisarErro(erro);
        }
    }

    carregar();
}

export async function telaCantina(area, aoSalvar) {
    let config;
    try {
        config = await obter("/api/cantina");
    } catch (erro) {
        avisarErro(erro);
        return;
    }
    const formulario = el("form", { classe: "formulario-largo", novalidate: true },
        campo("nome", "Nome da cantina", { valor: config.nome, obrigatorio: true }),
        campo("abertura", "Abre às", { tipo: "time", valor: config.abertura.substring(0, 5), obrigatorio: true }),
        campo("fechamento", "Fecha às", { tipo: "time", valor: config.fechamento.substring(0, 5), obrigatorio: true }),
        campo("taxaServico", "Taxa de serviço (%)", { tipo: "number", passo: "0.5", min: 0, max: 30, valor: Number(config.taxaServico) * 100 }),
        campo("exigirHorario", "Recusar pedidos fora do horário", { tipo: "checkbox", valor: config.exigirHorario }),
        el("button", { classe: "botao botao-principal", type: "submit" }, "Salvar alterações"));

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        const dados = lerFormulario(formulario);
        dados.taxaServico = Number(dados.taxaServico || 0) / 100;
        try {
            const salva = await substituir("/api/cantina", dados);
            avisar("Alterações salvas.", "sucesso");
            aoSalvar(salva);
        } catch (erro) {
            mostrarErrosDosCampos(formulario, erro);
        }
    });

    area.replaceChildren(
        el("div", { classe: "cabecalho-tela" },
            el("div", {},
                el("h2", {}, "Cantina"),
                el("p", { classe: "discreto" }, "Horário, taxa de serviço e regras de funcionamento."))),
        formulario);
}
