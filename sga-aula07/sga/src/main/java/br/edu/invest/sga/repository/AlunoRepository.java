package br.edu.invest.sga.repository;

import br.edu.invest.sga.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
