package br.edu.invest.sga.service;

import br.edu.invest.sga.exception.RegraNegocioException;
import br.edu.invest.sga.model.Aluno;
import br.edu.invest.sga.model.Curso;
import br.edu.invest.sga.model.Matricula;
import br.edu.invest.sga.model.StatusMatricula;
import br.edu.invest.sga.repository.AlunoRepository;
import br.edu.invest.sga.repository.CursoRepository;
import br.edu.invest.sga.repository.MatriculaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integracao (H2) para os Ex. 05, 06 e 07.
 * IMPORTANTE: a classe NAO e @Transactional, senao a transacao do proprio teste
 * mascararia o commit/rollback que queremos observar.
 */
@SpringBootTest
class MatriculaTransacaoTest {

    @Autowired MatriculaService matriculaService;
    @Autowired AlunoService alunoService;
    @Autowired CursoService cursoService;
    @Autowired MatriculaRepository matriculaRepository;
    @Autowired AlunoRepository alunoRepository;
    @Autowired CursoRepository cursoRepository;

    Aluno aluno;
    Curso cursoA;
    Curso cursoB;

    @BeforeEach
    void limpar() {
        matriculaRepository.deleteAll();
        alunoRepository.deleteAll();
        cursoRepository.deleteAll();
        aluno = alunoRepository.save(new Aluno(null, "Marco", "marco@email.com"));
        cursoA = cursoRepository.save(new Curso(null, "ADS", 2400));
        cursoB = cursoRepository.save(new Curso(null, "Ciência da Computação", 3200));
    }

    @Test
    @DisplayName("Ex.05: exceção não verificada -> ROLLBACK desfaz o cancelamento e a nova matrícula")
    void rollbackDesfazTodasAsAlteracoes() {
        Matricula original = matriculaService.matricular(aluno.getId(), cursoA.getId());

        assertThrows(IllegalStateException.class,
                () -> matriculaService.transferir(original.getId(), cursoB.getId(), true));

        Matricula depois = matriculaRepository.findById(original.getId()).orElseThrow();
        assertEquals(StatusMatricula.ATIVA, depois.getStatus());   // cancelamento foi revertido
        assertEquals(1, matriculaRepository.count());              // nova matricula nao existe
    }

    @Test
    @DisplayName("Ex.05: sem falha -> COMMIT confirma as duas alterações")
    void commitConfirmaTodasAsAlteracoes() {
        Matricula original = matriculaService.matricular(aluno.getId(), cursoA.getId());

        Matricula nova = matriculaService.transferir(original.getId(), cursoB.getId(), false);

        assertEquals(StatusMatricula.CANCELADA, matriculaRepository.findById(original.getId()).orElseThrow().getStatus());
        assertEquals(StatusMatricula.ATIVA, matriculaRepository.findById(nova.getId()).orElseThrow().getStatus());
        assertEquals(2, matriculaRepository.count());
    }

    @Test
    @DisplayName("Ex.06: dirty checking persiste o novo status sem chamar save()")
    void dirtyCheckingAtualizaSemSave() {
        Matricula m = matriculaService.matricular(aluno.getId(), cursoA.getId());

        matriculaService.trancarMatricula(m.getId());

        assertEquals(StatusMatricula.TRANCADA, matriculaRepository.findById(m.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Ex.07: não exclui aluno nem curso com matrícula ativa")
    void naoExcluiComMatriculaAtiva() {
        matriculaService.matricular(aluno.getId(), cursoA.getId());

        assertThrows(RegraNegocioException.class, () -> alunoService.excluir(aluno.getId()));
        assertThrows(RegraNegocioException.class, () -> cursoService.excluir(cursoA.getId()));
        assertTrue(alunoRepository.existsById(aluno.getId()));
        assertTrue(cursoRepository.existsById(cursoA.getId()));
    }

    @Test
    @DisplayName("Ex.07: exclui curso sem matrículas")
    void excluiCursoSemMatriculas() {
        cursoService.excluir(cursoB.getId());

        assertFalse(cursoRepository.existsById(cursoB.getId()));
    }

    @Test
    @DisplayName("Ex.07: matrícula não ativa mas existente -> FK do banco impede, com mensagem clara")
    void fkDoBancoImpedeExclusaoComHistorico() {
        Matricula m = matriculaService.matricular(aluno.getId(), cursoA.getId());
        matriculaService.cancelarMatricula(m.getId());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> alunoService.excluir(aluno.getId()));

        assertEquals("Não é possível excluir o aluno: existem matrículas (histórico) vinculadas a ele.", ex.getMessage());
    }
}
