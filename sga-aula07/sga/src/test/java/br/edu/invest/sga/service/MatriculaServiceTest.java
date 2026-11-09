package br.edu.invest.sga.service;

import br.edu.invest.sga.exception.RegraNegocioException;
import br.edu.invest.sga.model.Aluno;
import br.edu.invest.sga.model.Curso;
import br.edu.invest.sga.model.Matricula;
import br.edu.invest.sga.model.StatusMatricula;
import br.edu.invest.sga.repository.AlunoRepository;
import br.edu.invest.sga.repository.CursoRepository;
import br.edu.invest.sga.repository.MatriculaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private MatriculaService service;

    private final Aluno aluno = new Aluno(1L, "Marco", "marco@email.com");
    private final Curso curso = new Curso(2L, "ADS", 2400);

    private Matricula matriculaCom(StatusMatricula status) {
        return new Matricula(10L, aluno, curso, status);
    }

    // ---------- matricular ----------

    @Test
    @DisplayName("Aluno e curso existentes -> matrícula criada com status ATIVA")
    void deveCriarMatriculaQuandoAlunoECursoExistem() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.existsByAluno_IdAndCurso_IdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(false);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        Matricula resultado = service.matricular(1L, 2L);

        assertEquals(StatusMatricula.ATIVA, resultado.getStatus());
        assertSame(aluno, resultado.getAluno());
        assertSame(curso, resultado.getCurso());
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    @DisplayName("Aluno inexistente -> exceção")
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.matricular(1L, 2L));

        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Curso inexistente -> exceção")
    void deveLancarExcecaoQuandoCursoNaoExiste() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.matricular(1L, 2L));

        assertEquals("Curso não encontrado.", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Matrícula ativa duplicada -> operação rejeitada")
    void deveRejeitarMatriculaAtivaDuplicada() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.existsByAluno_IdAndCurso_IdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(true);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.matricular(1L, 2L));

        assertEquals("Aluno já possui matrícula ativa neste curso.", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    // ---------- trancar / cancelar / concluir ----------

    @Test
    @DisplayName("Trancamento válido -> status TRANCADA")
    void deveTrancarMatriculaAtiva() {
        Matricula m = matriculaCom(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        Matricula resultado = service.trancarMatricula(10L);

        assertEquals(StatusMatricula.TRANCADA, resultado.getStatus());
    }

    @Test
    @DisplayName("Cancelamento válido -> status CANCELADA")
    void deveCancelarMatriculaAtiva() {
        Matricula m = matriculaCom(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        Matricula resultado = service.cancelarMatricula(10L);

        assertEquals(StatusMatricula.CANCELADA, resultado.getStatus());
    }

    @Test
    @DisplayName("Conclusão válida -> status CONCLUIDA")
    void deveConcluirMatriculaAtiva() {
        Matricula m = matriculaCom(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        assertEquals(StatusMatricula.CONCLUIDA, service.concluirMatricula(10L).getStatus());
    }

    @Test
    @DisplayName("Conclusão inválida (matrícula cancelada) -> exceção e status preservado")
    void deveLancarExcecaoAoConcluirMatriculaCancelada() {
        Matricula m = matriculaCom(StatusMatricula.CANCELADA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        assertThrows(RegraNegocioException.class, () -> service.concluirMatricula(10L));

        assertEquals(StatusMatricula.CANCELADA, m.getStatus());
    }

    @Test
    @DisplayName("Trancar matrícula já concluída -> exceção")
    void deveLancarExcecaoAoTrancarMatriculaConcluida() {
        Matricula m = matriculaCom(StatusMatricula.CONCLUIDA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.trancarMatricula(10L));

        assertTrue(ex.getMessage().contains("CONCLUIDA"));
        assertEquals(StatusMatricula.CONCLUIDA, m.getStatus());
    }

    @Test
    @DisplayName("Matrícula inexistente -> exceção")
    void deveLancarExcecaoQuandoMatriculaNaoExiste() {
        when(matriculaRepository.findById(99L)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.cancelarMatricula(99L));

        assertEquals("Matrícula não encontrada.", ex.getMessage());
    }
}
