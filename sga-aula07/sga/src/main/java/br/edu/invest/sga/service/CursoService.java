package br.edu.invest.sga.service;

import br.edu.invest.sga.exception.RegraNegocioException;
import br.edu.invest.sga.model.Curso;
import br.edu.invest.sga.model.StatusMatricula;
import br.edu.invest.sga.repository.CursoRepository;
import br.edu.invest.sga.repository.MatriculaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    public CursoService(CursoRepository cursoRepository, MatriculaRepository matriculaRepository) {
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public Curso cadastrar(String nome, Integer cargaHoraria) {
        return cursoRepository.save(new Curso(null, nome, cargaHoraria));
    }

    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return cursoRepository.findAll();
    }

    // Ex. 07 - Exclusao de curso
    @Transactional
    public void excluir(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(RegraNegocioException::cursoNaoEncontrado);

        if (matriculaRepository.existsByCurso_IdAndStatus(id, StatusMatricula.ATIVA)) {
            throw RegraNegocioException.cursoComMatriculaAtiva();
        }

        try {
            cursoRepository.delete(curso);
            cursoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw RegraNegocioException.cursoComHistorico();
        }
    }
}
