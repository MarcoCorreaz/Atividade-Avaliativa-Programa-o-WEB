package br.edu.invest.sga.exception;

import br.edu.invest.sga.dto.error.CampoErroDTO;
import br.edu.invest.sga.dto.error.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @RestControllerAdvice permite centralizar o tratamento de exceções para múltiplos Controllers,
 * devolvendo respostas JSON padronizadas para toda a API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> recursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                                 HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(),
                request, List.of(), ex.getCodigo());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> regraNegocio(RegraNegocioException ex,
                                                         HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Regra de negócio", ex.getMessage(),
                request, List.of(), ex.getCodigo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> validacao(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        List<CampoErroDTO> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Erro de validação", "Existem campos inválidos.",
                request, campos, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> jsonInvalido(HttpMessageNotReadableException ex,
                                                         HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON inválido",
                "Não foi possível interpretar os dados enviados.", request, List.of(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        String mensagem = "O parâmetro '" + ex.getName() + "' possui um valor inválido.";
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido", mensagem,
                request, List.of(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> erroInterno(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado.", request, List.of(), null);
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String erro, String mensagem,
                                                     HttpServletRequest request, List<CampoErroDTO> campos,
                                                     String codigo) {
        ErroResponseDTO corpo = new ErroResponseDTO(LocalDateTime.now(), status.value(), erro,
                mensagem, request.getRequestURI(), campos, codigo);
        return ResponseEntity.status(status).body(corpo);
    }
}