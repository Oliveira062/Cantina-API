package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends CantinaException {

    public EstoqueInsuficienteException(String mensagem) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, mensagem);
    }

    public EstoqueInsuficienteException(String produto, int solicitado, int disponivel) {
        this(String.format("Estoque insuficiente para %s: solicitado %d, disponível %d. Reduza a quantidade.",
                produto, solicitado, disponivel));
    }
}
