const CHAVE_TOKEN = "cantina.token";

export class ErroApi extends Error {
    constructor(status, mensagem, campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos || {};
    }
}

export function tokenSalvo() {
    return sessionStorage.getItem(CHAVE_TOKEN);
}

export function salvarToken(token) {
    sessionStorage.setItem(CHAVE_TOKEN, token);
}

export function apagarToken() {
    sessionStorage.removeItem(CHAVE_TOKEN);
}

let aoPerderSessao = () => {};

export function quandoPerderSessao(funcao) {
    aoPerderSessao = funcao;
}

export async function api(metodo, caminho, corpo) {
    const cabecalhos = { Accept: "application/json" };
    const token = tokenSalvo();
    if (token) {
        cabecalhos.Authorization = "Bearer " + token;
    }
    if (corpo !== undefined) {
        cabecalhos["Content-Type"] = "application/json";
    }

    let resposta;
    try {
        resposta = await fetch(caminho, {
            method: metodo,
            headers: cabecalhos,
            body: corpo === undefined ? undefined : JSON.stringify(corpo)
        });
    } catch (erro) {
        throw new ErroApi(0, "Sem conexão com o servidor. Confira se a API está no ar.");
    }

    const texto = await resposta.text();
    const dados = texto ? JSON.parse(texto) : null;

    if (resposta.status === 401 && token) {
        apagarToken();
        aoPerderSessao();
    }
    if (!resposta.ok) {
        const mensagem = dados && dados.mensagem ? dados.mensagem : "Não foi possível concluir a operação.";
        throw new ErroApi(resposta.status, mensagem, dados ? dados.campos : null);
    }
    return dados;
}

export const obter = (caminho) => api("GET", caminho);
export const enviar = (caminho, corpo) => api("POST", caminho, corpo);
export const substituir = (caminho, corpo) => api("PUT", caminho, corpo);
export const alterar = (caminho, corpo) => api("PATCH", caminho, corpo);
export const remover = (caminho) => api("DELETE", caminho);
