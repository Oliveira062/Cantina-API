# Cantina Universitária - API REST

API do sistema de pedidos da cantina, feita com Java 21 e Spring Boot 4.2.
Usa PostgreSQL, JPA, Flyway, validação, autenticação JWT, Swagger e Docker.

## Subir com Docker (forma mais simples)

```bash
cp .env.example .env
docker compose up -d --build
```

- Interface web: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Saúde da API: http://localhost:8080/actuator/health
- Administrador inicial: o e-mail e a senha definidos em `ADMIN_EMAIL` e `ADMIN_SENHA` no `.env`

## Interface web

A interface fica em `src/main/resources/static` e é servida pela própria API, no mesmo endereço.
É feita com HTML, CSS e JavaScript puros, sem framework e sem etapa de build.

| Perfil | O que aparece |
|---|---|
| Cliente | Cardápio com a comanda do pedido, pagamento por PIX ou cartão, histórico dos pedidos e avisos |
| Atendente | Balcão (busca o cliente e monta o pedido, inclusive em dinheiro com troco), lista de pedidos e entrega |
| Cozinha | Fila com os pedidos para preparar e em preparo, atualizada a cada 10 segundos |
| Admin | Painel com contadores e estoque baixo, todas as telas acima, produtos, equipe e configuração da cantina |

## Rodar sem Docker (para desenvolver)

Com o JDK 21, o Maven 3.9 ou mais novo e um PostgreSQL local (banco `cantina`, usuário `cantina`, senha `cantina123`):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

No perfil `dev` o administrador é `admin@cantina.edu.br` / `Admin@2026`.

## Testes

```bash
mvn test
```

Os testes de ponta a ponta contra a API rodando ficam em `qa/cantina.yaml` e são executados
com a ferramenta `testador-api`:

```bash
ADMIN_SENHA=TroqueEstaSenha1 java -jar testador-api.jar qa/cantina.yaml
```

## Perfis e permissões

| Ação | Cliente | Atendente | Cozinha | Admin |
|---|:-:|:-:|:-:|:-:|
| Ver cardápio | sim | sim | sim | sim |
| Abrir pedido, itens, pagar | os próprios | para clientes | não | sim |
| Pagar em dinheiro | não | sim | não | sim |
| Iniciar preparo / marcar pronto | não | não | sim | sim |
| Confirmar entrega | não | sim | não | sim |
| Cancelar | antes de pagar | sim | não | sim |
| Produtos, usuários, configuração | não | não | não | sim |

## Onde estão os padrões do projeto original

| Padrão | Onde ficou |
|---|---|
| Singleton | `ConfiguracaoCantina` (uma única linha no banco) e os beans do Spring |
| Factory | `pagamento/PagamentoFactory` |
| Strategy | `pagamento/EstrategiaPagamento` e as classes `PagamentoPix`, `PagamentoCartao`, `PagamentoDinheiro` |
| Observer | `observer/ObservadorCliente` e `observer/ObservadorCozinha`, avisados por eventos do Spring |
