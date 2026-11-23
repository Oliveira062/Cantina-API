INSERT INTO configuracao_cantina (id, nome, taxa_servico, abertura, fechamento, exigir_horario)
VALUES (1, 'Cantina Universitária', 0, '07:00', '22:30', FALSE);

INSERT INTO produtos (nome, descricao, preco, quantidade_estoque) VALUES
    ('Coxinha', 'Frango com catupiry', 6.00, 20),
    ('Pão de queijo', 'Unidade grande', 4.50, 30),
    ('Suco natural', 'Laranja, 300 ml', 7.00, 15),
    ('Café', 'Expresso', 3.00, 40),
    ('Bolo de pote', 'Chocolate', 8.00, 0);
