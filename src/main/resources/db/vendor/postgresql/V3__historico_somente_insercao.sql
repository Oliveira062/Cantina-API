CREATE OR REPLACE FUNCTION bloquear_alteracao_historico()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'O histórico de pedidos não pode ser alterado nem apagado.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_historico_somente_insercao
BEFORE UPDATE OR DELETE ON historico_pedidos
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_historico();
