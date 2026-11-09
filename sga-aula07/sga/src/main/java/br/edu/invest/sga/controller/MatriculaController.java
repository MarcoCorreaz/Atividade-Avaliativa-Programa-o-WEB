package br.edu.invest.sga.controller;

import br.edu.invest.sga.dto.MatriculaRequest;
import br.edu.invest.sga.dto.MatriculaResponse;
import br.edu.invest.sga.dto.TransferenciaRequest;
import br.edu.invest.sga.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Controller: so HTTP, DTO e status. Nenhuma regra de negocio aqui. */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> matricular(@RequestBody @Valid MatriculaRequest req) {
        var matricula = service.matricular(req.alunoId(), req.cursoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(MatriculaResponse.de(matricula));
    }

    @GetMapping
    public List<MatriculaResponse> listar() {
        return service.listar().stream().map(MatriculaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public MatriculaResponse buscar(@PathVariable Long id) {
        return MatriculaResponse.de(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/trancar")
    public MatriculaResponse trancar(@PathVariable Long id) {
        return MatriculaResponse.de(service.trancarMatricula(id));
    }

    @PatchMapping("/{id}/cancelar")
    public MatriculaResponse cancelar(@PathVariable Long id) {
        return MatriculaResponse.de(service.cancelarMatricula(id));
    }

    @PatchMapping("/{id}/concluir")
    public MatriculaResponse concluir(@PathVariable Long id) {
        return MatriculaResponse.de(service.concluirMatricula(id));
    }

    @PostMapping("/{id}/transferir")
    public MatriculaResponse transferir(@PathVariable Long id, @RequestBody @Valid TransferenciaRequest req) {
        return MatriculaResponse.de(service.transferir(id, req.novoCursoId(), req.simularFalha()));
    }
}
