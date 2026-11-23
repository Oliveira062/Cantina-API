import { enviar, salvarToken } from "../api.js";
import { campo, el, lerFormulario, mostrarErrosDosCampos } from "../ui.js";

export function telaEntrada(raiz, cantina, aoEntrar, aviso) {
    const lado = el("div", { classe: "entrada-formulario" });
    const horario = cantina
        ? `Aberta das ${cantina.abertura.substring(0, 5)} às ${cantina.fechamento.substring(0, 5)}. Peça pelo celular e retire no balcão quando a cozinha avisar.`
        : "Peça pelo celular e retire no balcão quando a cozinha avisar.";

    raiz.replaceChildren(el("main", { classe: "entrada" },
        el("section", { classe: "entrada-parede" },
            el("p", { classe: "marca" }, cantina ? cantina.nome : "Cantina Universitária"),
            el("h1", {}, "Seu lanche sem fila no intervalo"),
            el("p", { classe: "horario" }, horario)),
        lado));

    mostrarLogin(lado, aoEntrar, aviso);
}

function mostrarLogin(lado, aoEntrar, aviso) {
    const formulario = el("form", { novalidate: true },
        el("h2", {}, "Entrar"),
        el("p", { classe: "discreto" }, aviso || "Use o e-mail e a senha da sua conta na cantina."),
        campo("email", "E-mail", { tipo: "email", autocompletar: "username", obrigatorio: true }),
        campo("senha", "Senha", { tipo: "password", autocompletar: "current-password", obrigatorio: true }),
        el("button", { classe: "botao botao-principal botao-largo", type: "submit" }, "Entrar"),
        el("p", { classe: "alternar discreto" }, "Ainda não tem conta? ",
            el("button", { classe: "link", type: "button", onclick: () => mostrarCadastro(lado, aoEntrar) }, "Criar conta")));

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        const botao = formulario.querySelector("[type=submit]");
        botao.disabled = true;
        try {
            const resposta = await enviar("/api/auth/login", lerFormulario(formulario));
            salvarToken(resposta.token);
            aoEntrar(resposta.usuario);
        } catch (erro) {
            mostrarErrosDosCampos(formulario, erro);
        } finally {
            botao.disabled = false;
        }
    });

    lado.replaceChildren(formulario);
    formulario.querySelector("input").focus();
}

function mostrarCadastro(lado, aoEntrar) {
    const formulario = el("form", { novalidate: true },
        el("h2", {}, "Criar conta"),
        el("p", { classe: "discreto" }, "A senha precisa ter pelo menos 8 caracteres, com letras e números."),
        campo("nome", "Nome", { autocompletar: "name", obrigatorio: true }),
        campo("email", "E-mail", { tipo: "email", autocompletar: "username", obrigatorio: true }),
        campo("matricula", "Matrícula (opcional)", { exemplo: "Somente números" }),
        campo("senha", "Senha", { tipo: "password", autocompletar: "new-password", obrigatorio: true }),
        el("button", { classe: "botao botao-principal botao-largo", type: "submit" }, "Criar conta"),
        el("p", { classe: "alternar discreto" }, "Já tem conta? ",
            el("button", { classe: "link", type: "button", onclick: () => mostrarLogin(lado, aoEntrar) }, "Entrar")));

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        const botao = formulario.querySelector("[type=submit]");
        botao.disabled = true;
        const dados = lerFormulario(formulario);
        try {
            await enviar("/api/auth/registrar", dados);
            const resposta = await enviar("/api/auth/login", { email: dados.email, senha: dados.senha });
            salvarToken(resposta.token);
            aoEntrar(resposta.usuario);
        } catch (erro) {
            mostrarErrosDosCampos(formulario, erro);
        } finally {
            botao.disabled = false;
        }
    });

    lado.replaceChildren(formulario);
    formulario.querySelector("input").focus();
}
