package br.edu.cantina.exception;

import org.springframework.http.HttpStatus;

public class ProdutoNaoEncontradoException extends CantinaException {

    public ProdutoNaoEncontradoException(Long codigo) {
        this(String.format("Produto de código %d não encontrado. Consulte o cardápio para ver os códigos válidos.", codigo));
    }

    public ProdutoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
