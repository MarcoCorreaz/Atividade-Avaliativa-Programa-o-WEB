package br.edu.invest.sga.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import br.edu.invest.sga.model.StatusMatricula;

public record MatriculaRequest(
                @NotNull(message = "Informe o aluno.") @Positive(message = "O aluno deve ser maior que zero.") Long alunoId,
                @NotNull(message = "Informe o curso.") @Positive(message = "O curso deve ser maior que zero.") Long cursoId,
                StatusMatricula status) {

        public MatriculaRequest(Long alunoId, Long cursoId) {
                this(alunoId, cursoId, null);
        }
}
