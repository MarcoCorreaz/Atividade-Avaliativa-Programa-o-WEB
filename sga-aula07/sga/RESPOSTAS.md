# Respostas teóricas – Aula 07

## Exercício 01 – Separação de responsabilidades

### 1. Responsabilidades de cada camada

| Camada | Responsabilidade |
|---|---|
| **Controller** | Receber e responder requisições HTTP: mapear rotas, converter JSON ⇄ DTO, acionar a validação estrutural (`@Valid`) e definir o status HTTP. Não consulta Repository e não decide regra. |
| **Service** | Aplicar as regras de negócio, coordenar operações entre vários repositories, lançar exceções de domínio e delimitar a transação (`@Transactional`). |
| **Repository** | Consultas e persistência (`save`, `findById`, `existsBy...`). Não decide regra de negócio. |
| **Banco de dados** | Armazenar os dados e garantir integridade estrutural (PK, FK, `NOT NULL`, `UNIQUE`). |

### 2. Por que a regra de negócio não deve ficar no Controller?

- O Controller é um adaptador HTTP: ele muda quando muda o contrato da API (rota, formato, status), e não quando muda a regra do negócio. Misturar os dois dá à classe mais de um motivo para mudar.
- **Reuso:** a mesma regra (ex.: "não matricular duas vezes") precisaria ser repetida em cada ponto de entrada (REST, fila, tarefa agendada, outro controller).
- **Transação:** a fronteira transacional deve acompanhar a operação de negócio, não a requisição HTTP; no Service ela cobre todas as etapas de uma vez.
- **Testabilidade:** regras no Service são testadas com JUnit + Mockito, sem subir servidor nem simular requisições.

### 3. Qual camada verifica se o aluno já tem matrícula ativa no curso?

O **Service**. Ele pergunta ao Repository (`existsByAluno_IdAndCurso_IdAndStatus`), mas é o Service quem *decide* que existir significa violação e lança `RegraNegocioException`. O Repository só responde a consulta; o Controller nunca decide.

### 4. Fluxo de uma requisição de matrícula até o banco

```
Cliente --POST /matriculas {alunoId, cursoId}--> Controller
Controller: desserializa em MatriculaRequest, valida (@Valid) e chama service.matricular(...)
   Service (@Transactional - abre a transação)
     1. alunoRepository.findById(alunoId)    -> senão: RegraNegocioException "Aluno não encontrado."
     2. cursoRepository.findById(cursoId)    -> senão: RegraNegocioException "Curso não encontrado."
     3. matriculaRepository.existsBy...ATIVA -> se true: RegraNegocioException (duplicidade)
     4. matriculaRepository.save(new Matricula(aluno, curso, ATIVA))
        Repository -> INSERT em tb_matricula -> Banco
   Service: fim do método -> COMMIT (ou ROLLBACK se saiu exceção não verificada)
Controller: converte a entidade em MatriculaResponse e devolve 201 Created
```

Diagrama simplificado:

```
 HTTP ──► [Controller] ──► [Service] ──► [Repository] ──► [Banco]
          HTTP/DTO/status   regras +      consultas +       integridade
                            transações    persistência      e dados
 HTTP ◄── resposta (201/404/409) ◄───────────────────────────────┘
```

---

## Exercício 05 – Transações com @Transactional

### 1. Objetivo da anotação

`@Transactional` delimita a fronteira de uma unidade de negócio: todas as operações de banco feitas dentro do método ocorrem na **mesma transação** (atomicidade). Ou tudo é confirmado, ou tudo é desfeito.

### 2. Método onde foi aplicado

`MatriculaService#transferir`, que faz **duas alterações**: cancela a matrícula atual e cria uma nova matrícula em outro curso. Os demais métodos de escrita também usam `@Transactional`; as consultas usam `@Transactional(readOnly = true)`.

### 3. Exceção não verificada simulada

Com `simularFalha = true`, o método lança `IllegalStateException` **depois** de cancelar a matrícula original (com `flush()`, ou seja, o `UPDATE` já foi enviado ao banco) e de salvar a nova.

### 4. Verificação do rollback

O teste `MatriculaTransacaoTest#rollbackDesfazTodasAsAlteracoes` confirma que, após a exceção:
- a matrícula original continua **ATIVA** (o cancelamento foi revertido);
- só existe 1 matrícula no banco (a nova não foi gravada).

O teste `commitConfirmaTodasAsAlteracoes` mostra o caso oposto: sem falha, a original fica **CANCELADA** e a nova **ATIVA**.
(Detalhe: a classe de teste não é `@Transactional`, senão a transação do próprio teste esconderia o commit/rollback do Service.)

Resultado esperado ao rodar `mvn test`: ambos os testes passam.

### 5. Commit × Rollback

- **Commit:** a transação terminou sem erro; todas as alterações são confirmadas e tornam-se definitivas e visíveis para outras transações.
- **Rollback:** houve uma falha; todas as alterações feitas desde o início da transação são desfeitas, e o banco volta ao estado anterior.

Obs.: por padrão o Spring faz rollback para exceções **não verificadas** (`RuntimeException` e `Error`). Exceções verificadas (`Exception`) **não** causam rollback, a menos que se use `@Transactional(rollbackFor = ...)`. Por isso `RegraNegocioException` estende `RuntimeException`.

---

## Exercício 06 – Dirty checking

### Por que o JPA atualiza a entidade sem `save()`?

Dentro de uma transação, a entidade carregada com `findById` é **gerenciada** pelo `EntityManager` (contexto de persistência). Ao carregá-la, o Hibernate guarda um *snapshot* do estado original. No `flush`/commit ele compara o estado atual com o snapshot; se algum atributo mudou (aqui, `status`), ele detecta a alteração (**dirty checking**) e gera o `UPDATE` automaticamente.

```java
@Transactional
public Matricula cancelarMatricula(Long id) {
    Matricula m = repository.findById(id).orElseThrow(...); // 1. carrega (gerenciada)
    m.setStatus(StatusMatricula.CANCELADA);                 // 2. altera o atributo
    return m;                                               // 3-4. no commit: detecta e gera UPDATE
}
```

Condições: a entidade precisa estar gerenciada e a alteração precisa ocorrer dentro da transação. Fora dela (entidade *detached*), a mudança seria perdida sem um `save()`/`merge()`.
O teste `dirtyCheckingAtualizaSemSave` comprova isso.
