package br.edu.cantina;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracaoTest {

    private static final String SENHA = "Senha123";

    @Autowired
    private MockMvc mvc;

    private String tokenAdmin;

    @BeforeEach
    void entrarComoAdmin() throws Exception {
        tokenAdmin = login("admin@teste.com", "Admin@2026");
    }

    @Test
    @DisplayName("Rota protegida sem token responde 401")
    void semToken() throws Exception {
        mvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Token adulterado responde 401")
    void tokenAdulterado() throws Exception {
        String token = novoCliente();
        String adulterado = token.substring(0, token.length() - 3) + "abc";
        mvc.perform(get("/api/pedidos").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cardápio é público e não mostra produtos desativados")
    void cardapioPublico() throws Exception {
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Coxinha")));
    }

    @Test
    @DisplayName("Cadastro público sempre cria CLIENTE, mesmo se enviarem outro perfil")
    void registroNaoEscolhePerfil() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/auth/registrar").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Esperto", "email": "%s", "senha": "%s", "perfil": "ADMIN"}
                        """.formatted(email, SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    @DisplayName("Validação devolve a mensagem de cada campo inválido")
    void validacaoDeCampos() throws Exception {
        mvc.perform(post("/api/auth/registrar").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Jo", "email": "nao-e-email", "senha": "curta"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    @DisplayName("Cliente não pode cadastrar produto")
    void clienteNaoCadastraProduto() throws Exception {
        String token = novoCliente();
        mvc.perform(post("/api/produtos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Pastel", "descricao": "Carne", "preco": 7.50, "quantidadeInicial": 10}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cliente não enxerga o pedido de outro cliente")
    void isolamentoEntreClientes() throws Exception {
        String ana = novoCliente();
        String bruno = novoCliente();
        long numero = criarPedido(ana);

        mvc.perform(get("/api/pedidos/" + numero).header("Authorization", "Bearer " + bruno))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/pedidos/" + numero + "/cancelar").header("Authorization", "Bearer " + bruno))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos").header("Authorization", "Bearer " + bruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Cliente não registra pagamento em dinheiro")
    void clienteNaoPagaEmDinheiro() throws Exception {
        String token = novoCliente();
        long numero = criarPedido(token);
        adicionarItem(token, numero, 2, 1);
        mvc.perform(post("/api/pedidos/" + numero + "/pagamento").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "DINHEIRO", "valorRecebido": 50}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("balcão")));
    }

    @Test
    @DisplayName("Fluxo completo: cliente pede, cozinha prepara, atendente entrega")
    void fluxoCompleto() throws Exception {
        String cliente = novoCliente();
        String cozinha = novoFuncionario("COZINHA");
        String atendente = novoFuncionario("ATENDENTE");

        long numero = criarPedido(cliente);
        adicionarItem(cliente, numero, 4, 2).andExpect(jsonPath("$.total").value(6.0));

        mvc.perform(post("/api/pedidos/" + numero + "/pagamento").header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "pix"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(true))
                .andExpect(jsonPath("$.pedido.situacao").value("PAGO"));

        mvc.perform(post("/api/pedidos/" + numero + "/entrega").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/cozinha/fila").header("Authorization", "Bearer " + cozinha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].numero", hasItem((int) numero)));

        mvc.perform(post("/api/pedidos/" + numero + "/preparo").header("Authorization", "Bearer " + cozinha))
                .andExpect(status().isOk());
        mvc.perform(post("/api/pedidos/" + numero + "/pronto").header("Authorization", "Bearer " + cozinha))
                .andExpect(status().isOk());
        mvc.perform(post("/api/pedidos/" + numero + "/entrega").header("Authorization", "Bearer " + atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ENTREGUE"));

        mvc.perform(get("/api/pedidos/" + numero + "/historico").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));

        mvc.perform(get("/api/notificacoes").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensagem", containsString("Entregue")));
    }

    @Test
    @DisplayName("Depois de 5 senhas erradas o login fica bloqueado")
    void bloqueioDeLogin() throws Exception {
        String email = emailUnico();
        registrar(email);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                            {"email": "%s", "senha": "errada123"}
                            """.formatted(email)))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "senha": "%s"}
                        """.formatted(email, SENHA)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Conta desativada perde o acesso na hora, mesmo com token válido")
    void contaDesativada() throws Exception {
        String email = emailUnico();
        long id = registrar(email);
        String token = login(email, SENHA);

        mvc.perform(patch("/api/usuarios/" + id + "/status").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ativo": false}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("A interface web é pública e sai com a política de segurança de conteúdo")
    void interfacePublica() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
        mvc.perform(get("/js/app.js")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Só atendente e admin buscam clientes no balcão")
    void buscaDeClientes() throws Exception {
        String email = emailUnico();
        registrar(email);
        String cliente = login(email, SENHA);
        String atendente = novoFuncionario("ATENDENTE");

        mvc.perform(get("/api/clientes").param("busca", email).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/clientes").param("busca", email).header("Authorization", "Bearer " + atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].perfil").value("CLIENTE"));
    }

    private String novoCliente() throws Exception {
        String email = emailUnico();
        registrar(email);
        return login(email, SENHA);
    }

    private String novoFuncionario(String perfil) throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Funcionário", "email": "%s", "senha": "%s", "perfil": "%s"}
                                """.formatted(email, SENHA, perfil)))
                .andExpect(status().isCreated());
        return login(email, SENHA);
    }

    private long registrar(String email) throws Exception {
        String resposta = mvc.perform(post("/api/auth/registrar").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Cliente Teste", "email": "%s", "senha": "%s", "matricula": "2024001"}
                        """.formatted(email, SENHA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String login(String email, String senha) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "senha": "%s"}
                        """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private long criarPedido(String token) throws Exception {
        String resposta = mvc.perform(post("/api/pedidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.numero")).longValue();
    }

    private ResultActions adicionarItem(String token, long numero, long produto, int quantidade) throws Exception {
        return mvc.perform(post("/api/pedidos/" + numero + "/itens").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigoProduto": %d, "quantidade": %d}
                                """.formatted(produto, quantidade)))
                .andExpect(status().isOk());
    }

    private String emailUnico() {
        return "teste-" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }
}
