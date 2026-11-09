package br.edu.invest.sga.exception;

import br.edu.invest.sga.model.StatusMatricula;

/**
 * Violacao de regra de negocio do Sistema Academico.
 * Excecao nao verificada (RuntimeException): por padrao o Spring faz ROLLBACK da transacao.
 */
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String mensagem) {
        this(mensagem, null);
    }

    public RegraNegocioException(String mensagem, String codigo) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    // --- Cenarios exigidos no Exercicio 03 ---

    public static RegraNegocioException alunoNaoEncontrado() {
        return new RecursoNaoEncontradoException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO");
    }

    public static RegraNegocioException cursoNaoEncontrado() {
        return new RecursoNaoEncontradoException("Curso não encontrado.", "CURSO_NAO_ENCONTRADO");
    }

    public static RegraNegocioException matriculaAtivaJaExistente() {
        return new RegraNegocioException("Aluno já possui matrícula ativa neste curso.", "MATRICULA_ATIVA_DUPLICADA");
    }

    public static RegraNegocioException operacaoIncompativel(String operacao, StatusMatricula atual) {
        return new RegraNegocioException(
                "Operação incompatível com o status atual da matrícula: não é possível "
                        + operacao + " uma matrícula com status " + atual + ".");
    }

    // --- Demais cenarios do SGA ---

    public static RegraNegocioException matriculaNaoEncontrada() {
        return new RecursoNaoEncontradoException("Matrícula não encontrada.", "MATRICULA_NAO_ENCONTRADA");
    }

    public static RegraNegocioException alunoComMatriculaAtiva() {
        return new RegraNegocioException("Não é possível excluir o aluno: ele possui matrícula ativa.");
    }

    public static RegraNegocioException cursoComMatriculaAtiva() {
        return new RegraNegocioException("Não é possível excluir o curso: ele possui matrícula ativa.");
    }

    public static RegraNegocioException alunoComHistorico() {
        return new RegraNegocioException("Não é possível excluir o aluno: existem matrículas (histórico) vinculadas a ele.");
    }

    public static RegraNegocioException cursoComHistorico() {
        return new RegraNegocioException("Não é possível excluir o curso: existem matrículas (histórico) vinculadas a ele.");
    }
}
