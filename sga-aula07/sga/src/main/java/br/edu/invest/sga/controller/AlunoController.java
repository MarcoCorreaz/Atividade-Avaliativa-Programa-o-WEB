package br.edu.invest.sga.controller;

import br.edu.invest.sga.dto.AlunoRequest;
import br.edu.invest.sga.model.Aluno;
import br.edu.invest.sga.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Aluno cadastrar(@RequestBody @Valid AlunoRequest req) {
        return service.cadastrar(req.nome(), req.email());
    }

    @GetMapping
    public List<Aluno> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Aluno consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
