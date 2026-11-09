package br.edu.invest.sga.controller;

import br.edu.invest.sga.dto.CursoRequest;
import br.edu.invest.sga.model.Curso;
import br.edu.invest.sga.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Curso cadastrar(@RequestBody @Valid CursoRequest req) {
        return service.cadastrar(req.nome(), req.cargaHoraria());
    }

    @GetMapping
    public List<Curso> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
