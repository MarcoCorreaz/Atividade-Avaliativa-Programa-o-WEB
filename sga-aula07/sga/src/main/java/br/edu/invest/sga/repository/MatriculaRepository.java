package br.edu.invest.sga.repository;

import br.edu.invest.sga.model.Matricula;
import br.edu.invest.sga.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAluno_IdAndCurso_IdAndStatus(Long alunoId, Long cursoId, StatusMatricula status);

    boolean existsByAluno_IdAndStatus(Long alunoId, StatusMatricula status);

    boolean existsByCurso_IdAndStatus(Long cursoId, StatusMatricula status);
}
