package br.edu.invest.sga.exception;

/** Recurso inexistente. E uma RegraNegocioException, mas mapeada para HTTP 404. */
public class RecursoNaoEncontradoException extends RegraNegocioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String mensagem, String codigo) {
        super(mensagem, codigo);
    }
}
