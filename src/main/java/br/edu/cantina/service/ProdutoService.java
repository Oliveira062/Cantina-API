package br.edu.cantina.service;

import br.edu.cantina.dto.AtualizarProdutoRequest;
import br.edu.cantina.dto.ProdutoRequest;
import br.edu.cantina.dto.ProdutoResponse;
import br.edu.cantina.exception.ProdutoNaoEncontradoException;
import br.edu.cantina.model.Produto;
import br.edu.cantina.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarCardapio(boolean incluirInativos) {
        List<Produto> produtos = incluirInativos
                ? repository.findAllByOrderByNomeAsc()
                : repository.findByAtivoTrueOrderByNomeAsc();
        return produtos.stream().map(ProdutoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse consultar(Long codigo, boolean incluirInativos) {
        Produto produto = buscar(codigo);
        if (!produto.isAtivo() && !incluirInativos) {
            throw new ProdutoNaoEncontradoException(codigo);
        }
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest dados) {
        Produto produto = new Produto(dados.nome(), dados.descricao(), dados.preco(), dados.quantidadeInicial());
        return ProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizarDados(Long codigo, AtualizarProdutoRequest dados) {
        Produto produto = buscar(codigo);
        produto.atualizarDados(dados.nome(), dados.descricao());
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse atualizarPreco(Long codigo, BigDecimal novoPreco) {
        Produto produto = buscar(codigo);
        produto.atualizarPreco(novoPreco);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse reporEstoque(Long codigo, int quantidade) {
        Produto produto = buscar(codigo);
        produto.reporEstoque(quantidade);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse alterarAtivo(Long codigo, boolean ativo) {
        Produto produto = buscar(codigo);
        produto.setAtivo(ativo);
        return ProdutoResponse.de(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscar(Long codigo) {
        return repository.findById(codigo).orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));
    }
}
