package br.edu.invest.sga.dto;

import br.edu.invest.sga.model.Matricula;
import br.edu.invest.sga.model.StatusMatricula;

public record MatriculaResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long cursoId,
        String cursoNome,
        StatusMatricula status) {

    public static MatriculaResponse de(Matricula m) {
        return new MatriculaResponse(
                m.getId(),
                m.getAluno().getId(), m.getAluno().getNome(),
                m.getCurso().getId(), m.getCurso().getNome(),
                m.getStatus());
    }
}
