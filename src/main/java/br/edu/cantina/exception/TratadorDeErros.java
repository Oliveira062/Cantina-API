package br.edu.cantina.exception;

import br.edu.cantina.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(CantinaException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(CantinaException e, HttpServletRequest request) {
        return montar(e.getStatus(), e.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return montar(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição.", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou com formato inválido.",
                request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException e,
                                                          HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.", request, null);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErroResponse> acessoNegado(RuntimeException e, HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação.", request, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> concorrencia(OptimisticLockingFailureException e, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT,
                "Outro usuário alterou estes dados ao mesmo tempo. Consulte novamente e repita a operação.",
                request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(), e.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest request) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado nesta rota.", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException e, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception e, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente em instantes.", request, null);
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, HttpServletRequest request,
                                                Map<String, String> campos) {
        ErroResponse corpo = new ErroResponse(Instant.now(), status.value(), status.getReasonPhrase(), mensagem,
                request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
