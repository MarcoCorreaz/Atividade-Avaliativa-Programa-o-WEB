# SGA - Sistema de Gerenciamento Acadêmico

Projeto da disciplina Programação Web II, com Spring Boot 3.3, Java 17, Spring Data JPA, validação Jakarta e PostgreSQL/H2.

## Funcionalidades

- Cadastro e consulta de alunos e cursos.
- Cadastro, consulta, transição de status e transferência de matrículas.
- Regras de negócio e transações com `@Transactional`.
- Tratamento global de exceções com respostas JSON padronizadas.
- Testes unitários com Mockito e testes web com MockMvc.

## Como executar

Pré-requisitos: JDK 17+ e Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

O perfil padrão usa H2 em memória. Para usar PostgreSQL, ajuste somente o arquivo
`src/main/resources/application-postgres.properties` e execute:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Estrutura

```text
controller  -> endpoints HTTP e conversão de requests
service     -> regras de negócio e transações
repository  -> consultas e persistência
model       -> entidades e enum StatusMatricula
dto         -> requests e responses
dto.error   -> respostas padronizadas de erro
exception   -> exceções de domínio e GlobalExceptionHandler
```

## Endpoints

Todas as rotas usam o prefixo `/api`.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/alunos` | Cadastra aluno |
| GET | `/api/alunos` | Lista alunos |
| GET | `/api/alunos/{id}` | Consulta aluno |
| DELETE | `/api/alunos/{id}` | Exclui aluno |
| POST | `/api/cursos` | Cadastra curso |
| GET | `/api/cursos` | Lista cursos |
| DELETE | `/api/cursos/{id}` | Exclui curso |
| POST | `/api/matriculas` | Cria matrícula |
| GET | `/api/matriculas` | Lista matrículas |
| GET | `/api/matriculas/{id}` | Consulta matrícula |
| PATCH | `/api/matriculas/{id}/trancar` | Tranca matrícula |
| PATCH | `/api/matriculas/{id}/cancelar` | Cancela matrícula |
| PATCH | `/api/matriculas/{id}/concluir` | Conclui matrícula |
| POST | `/api/matriculas/{id}/transferir` | Transfere matrícula |

## Respostas de erro

O `GlobalExceptionHandler` centraliza as respostas para todos os controllers.
O formato contém `timestamp`, `status`, `erro`, `mensagem`, `path` e `campos`.
Erros de domínio também podem informar um `codigo` estável, como `ALUNO_NAO_ENCONTRADO`.

| Situação | Status |
|---|---:|
| Recurso não encontrado | 404 |
| Regra de negócio | 400 |
| DTO inválido | 400 |
| JSON inválido | 400 |
| Parâmetro incompatível | 400 |
| Erro inesperado | 500 |

Erros inesperados são registrados no log do servidor, mas a resposta não expõe SQL,
stack trace, nomes de classes ou outros detalhes internos.

## Documentação e respostas da atividade

O roteiro da Semana 09 está em [RESPOSTAS-SEMANA-09.md](RESPOSTAS-SEMANA-09.md).
Springdoc/Swagger não foi adicionado porque isso exigiria uma dependência externa ainda não autorizada.

## Exercícios anteriores

As respostas e explicações das atividades de serviço e transação permanecem em [RESPOSTAS.md](RESPOSTAS.md).
