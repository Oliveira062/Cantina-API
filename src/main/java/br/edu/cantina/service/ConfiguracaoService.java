package br.edu.cantina.service;

import br.edu.cantina.config.PropriedadesCantina;
import br.edu.cantina.dto.ConfiguracaoRequest;
import br.edu.cantina.dto.ConfiguracaoResponse;
import br.edu.cantina.exception.DadosInvalidosException;
import br.edu.cantina.exception.OperacaoPedidoInvalidaException;
import br.edu.cantina.model.ConfiguracaoCantina;
import br.edu.cantina.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;
    private final ZoneId fuso;

    public ConfiguracaoService(ConfiguracaoRepository repository, PropriedadesCantina propriedades) {
        this.repository = repository;
        this.fuso = ZoneId.of(propriedades.fusoHorario());
    }

    @Transactional(readOnly = true)
    public ConfiguracaoCantina obter() {
        return repository.findById(ConfiguracaoCantina.ID_UNICO)
                .orElseThrow(() -> new IllegalStateException("Configuração da cantina não encontrada no banco."));
    }

    @Transactional(readOnly = true)
    public BigDecimal taxaServico() {
        return obter().getTaxaServico();
    }

    @Transactional(readOnly = true)
    public ConfiguracaoResponse consultar() {
        return paraResposta(obter());
    }

    @Transactional
    public ConfiguracaoResponse atualizar(ConfiguracaoRequest dados) {
        if (!dados.abertura().isBefore(dados.fechamento())) {
            throw new DadosInvalidosException("O horário de abertura deve ser antes do fechamento.");
        }
        ConfiguracaoCantina config = obter();
        config.atualizar(dados.nome(), dados.taxaServico(), dados.abertura(), dados.fechamento(),
                dados.exigirHorario());
        return paraResposta(repository.save(config));
    }

    @Transactional(readOnly = true)
    public void verificarFuncionamento() {
        ConfiguracaoCantina config = obter();
        if (config.isExigirHorario() && !config.estaAberta(LocalTime.now(fuso))) {
            throw new OperacaoPedidoInvalidaException(String.format(
                    "A cantina está fechada. Horário de funcionamento: %s às %s.",
                    config.getAbertura(), config.getFechamento()));
        }
    }

    private ConfiguracaoResponse paraResposta(ConfiguracaoCantina config) {
        return new ConfiguracaoResponse(config.getNome(), config.getTaxaServico(), config.getAbertura(),
                config.getFechamento(), config.isExigirHorario(), config.estaAberta(LocalTime.now(fuso)));
    }
}
