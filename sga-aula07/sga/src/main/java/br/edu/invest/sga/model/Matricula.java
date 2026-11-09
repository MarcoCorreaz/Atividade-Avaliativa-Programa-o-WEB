package br.edu.invest.sga.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matricula_aluno"))
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matricula_curso"))
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusMatricula status;

    protected Matricula() {
    }

    public Matricula(Long id, Aluno aluno, Curso curso, StatusMatricula status) {
        this.id = id;
        this.aluno = aluno;
        this.curso = curso;
        this.status = status;
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Curso getCurso() { return curso; }
    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }
}
