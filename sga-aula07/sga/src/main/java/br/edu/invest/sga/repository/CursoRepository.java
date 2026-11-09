package br.edu.invest.sga.repository;

import br.edu.invest.sga.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
