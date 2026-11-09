package br.edu.invest.sga.service;

import br.edu.invest.sga.exception.RegraNegocioException;
import br.edu.invest.sga.model.Aluno;
import br.edu.invest.sga.model.Curso;
import br.edu.invest.sga.model.Matricula;
import br.edu.invest.sga.model.StatusMatricula;
import br.edu.invest.sga.repository.AlunoRepository;
import br.edu.invest.sga.repository.CursoRepository;
import br.edu.invest.sga.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            AlunoRepository alunoRepository,
                            CursoRepository cursoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
    }

    // ------------------------------------------------------------------
    // Ex. 02 - Validacao de matricula
    // ------------------------------------------------------------------
    @Transactional
    public Matricula matricular(Long alunoId, Long cursoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(RegraNegocioException::alunoNaoEncontrado);          // 1. aluno existe?
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(RegraNegocioException::cursoNaoEncontrado);          // 2. curso existe?

        validarDuplicidade(alunoId, cursoId);                                      // 3. sem duplicidade ativa

        return matriculaRepository.save(new Matricula(null, aluno, curso, StatusMatricula.ATIVA)); // 4.
    }

    @Transactional(readOnly = true)
    public List<Matricula> listar() {
        return matriculaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Matricula buscarPorId(Long id) {
        return buscar(id);
    }

    // ------------------------------------------------------------------
    // Ex. 04 + Ex. 06 - Estados da matricula com dirty checking
    // Regra: so ATIVA pode ir para TRANCADA, CANCELADA ou CONCLUIDA.
    // Nao ha chamada a save(): a entidade e gerenciada e o Hibernate
    // gera o UPDATE sozinho no commit (dirty checking).
    // ------------------------------------------------------------------
    @Transactional
    public Matricula trancarMatricula(Long id) {
        return transicionar(id, StatusMatricula.TRANCADA, "trancar");
    }

    @Transactional
    public Matricula cancelarMatricula(Long id) {
        return transicionar(id, StatusMatricula.CANCELADA, "cancelar");
    }

    @Transactional
    public Matricula concluirMatricula(Long id) {
        return transicionar(id, StatusMatricula.CONCLUIDA, "concluir");
    }

    // ------------------------------------------------------------------
    // Ex. 05 - Transacao envolvendo duas alteracoes no banco
    // Cancela a matricula atual E cria uma nova em outro curso.
    // Se algo falhar no meio, as duas operacoes sao desfeitas (rollback).
    // ------------------------------------------------------------------
    @Transactional
    public Matricula transferir(Long matriculaId, Long novoCursoId, boolean simularFalha) {
        Matricula atual = buscar(matriculaId);
        exigirAtiva(atual, "transferir");

        Curso novoCurso = cursoRepository.findById(novoCursoId)
                .orElseThrow(RegraNegocioException::cursoNaoEncontrado);
        validarDuplicidade(atual.getAluno().getId(), novoCursoId);

        atual.setStatus(StatusMatricula.CANCELADA);   // alteracao 1 (dirty checking)
        matriculaRepository.flush();                  // forca o UPDATE agora, para provar que o rollback o desfaz

        Matricula nova = matriculaRepository.save(    // alteracao 2
                new Matricula(null, atual.getAluno(), novoCurso, StatusMatricula.ATIVA));

        if (simularFalha) {
            // excecao NAO verificada -> rollback automatico das alteracoes 1 e 2
            throw new IllegalStateException("Falha simulada durante a transferência de matrícula.");
        }
        return nova;
    }

    // ------------------------------------------------------------------
    // helpers privados
    // ------------------------------------------------------------------
    private Matricula transicionar(Long id, StatusMatricula novoStatus, String verbo) {
        Matricula matricula = buscar(id);          // entidade gerenciada
        exigirAtiva(matricula, verbo);             // valida a transicao
        matricula.setStatus(novoStatus);           // dirty checking cuida do UPDATE
        return matricula;
    }

    private Matricula buscar(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(RegraNegocioException::matriculaNaoEncontrada);
    }

    private void exigirAtiva(Matricula matricula, String verbo) {
        if (matricula.getStatus() != StatusMatricula.ATIVA) {
            throw RegraNegocioException.operacaoIncompativel(verbo, matricula.getStatus());
        }
    }

    private void validarDuplicidade(Long alunoId, Long cursoId) {
        boolean existe = matriculaRepository.existsByAluno_IdAndCurso_IdAndStatus(
                alunoId, cursoId, StatusMatricula.ATIVA);
        if (existe) {
            throw RegraNegocioException.matriculaAtivaJaExistente();
        }
    }
}
