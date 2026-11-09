package br.edu.invest.sga.exception;

import br.edu.invest.sga.controller.AlunoController;
import br.edu.invest.sga.controller.MatriculaController;
import br.edu.invest.sga.service.AlunoService;
import br.edu.invest.sga.service.MatriculaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AlunoController.class, MatriculaController.class})
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlunoService alunoService;

    @MockBean
    private MatriculaService matriculaService;

    @Test
    void deveRetornar404PadronizadoParaAlunoInexistente() throws Exception {
        when(alunoService.consultar(99999L)).thenThrow(
                new RecursoNaoEncontradoException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        mockMvc.perform(get("/api/alunos/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.path").value("/api/alunos/99999"))
                .andExpect(jsonPath("$.campos", hasSize(0)))
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"));
    }

    @Test
    void deveRetornar400ParaDuplicidadeDeMatricula() throws Exception {
        when(matriculaService.matricular(1L, 2L)).thenThrow(
                RegraNegocioException.matriculaAtivaJaExistente());

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":1,\"cursoId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Regra de negócio"))
                .andExpect(jsonPath("$.mensagem")
                        .value("Aluno já possui matrícula ativa neste curso."));
    }

    @Test
    void deveRetornarCamposInvalidosNoCadastroDeAluno() throws Exception {
        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"email-invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos."))
                .andExpect(jsonPath("$.campos", hasSize(2)))
                .andExpect(jsonPath("$.campos[*].campo", containsInAnyOrder("nome", "email")));
    }

    @Test
    void deveOcultarDetalhesDeEnumInvalido() throws Exception {
        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":1,\"cursoId\":2,\"status\":\"ABERTA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("Não foi possível interpretar os dados enviados."))
                .andExpect(content().string(not(containsString("StatusMatricula"))))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void deveRetornar400ParaParametroInvalido() throws Exception {
        mockMvc.perform(get("/api/alunos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro inválido"))
                .andExpect(jsonPath("$.mensagem")
                        .value("O parâmetro 'id' possui um valor inválido."));
    }

    @Test
    void deveOcultarDetalhesDeErroInterno() throws Exception {
        when(alunoService.consultar(99999L)).thenThrow(
                new RuntimeException("select * from tb_aluno where senha = 'secreta'"));

        mockMvc.perform(get("/api/alunos/99999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Ocorreu um erro inesperado."))
                .andExpect(content().string(not(containsString("select"))))
                .andExpect(content().string(not(containsString("tb_aluno"))))
                .andExpect(content().string(not(containsString("senha"))));
    }

    @Test
    void deveOmitirCodigoQuandoExcecaoNaoInformarCodigo() throws Exception {
        when(matriculaService.buscarPorId(999L)).thenThrow(
                new RecursoNaoEncontradoException("Matrícula não encontrada."));

        mockMvc.perform(get("/api/matriculas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.campos", hasSize(0)))
                .andExpect(jsonPath("$.codigo").doesNotExist());
    }
}