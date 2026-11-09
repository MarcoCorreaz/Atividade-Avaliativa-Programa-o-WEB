package br.edu.invest.sga.service;

import br.edu.invest.sga.exception.RegraNegocioException;
import br.edu.invest.sga.model.Aluno;
import br.edu.invest.sga.model.StatusMatricula;
import br.edu.invest.sga.repository.AlunoRepository;
import br.edu.invest.sga.repository.MatriculaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;

    public AlunoService(AlunoRepository alunoRepository, MatriculaRepository matriculaRepository) {
        this.alunoRepository = alunoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public Aluno cadastrar(String nome, String email) {
        return alunoRepository.save(new Aluno(null, nome, email));
    }

    @Transactional(readOnly = true)
    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Aluno consultar(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(RegraNegocioException::alunoNaoEncontrado);
    }

    // Ex. 07 - Exclusao de aluno
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(RegraNegocioException::alunoNaoEncontrado);

        // regra de dominio (mensagem clara)
        if (matriculaRepository.existsByAluno_IdAndStatus(id, StatusMatricula.ATIVA)) {
            throw RegraNegocioException.alunoComMatriculaAtiva();
        }

        // integridade do banco (FK): flush() forca o DELETE aqui dentro do try,
        // senao o erro so apareceria no commit, fora do metodo.
        try {
            alunoRepository.delete(aluno);
            alunoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw RegraNegocioException.alunoComHistorico();
        }
    }
}
