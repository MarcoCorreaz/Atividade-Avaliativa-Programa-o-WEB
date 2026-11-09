# Respostas da Semana 09

## A) Atividade 34

| Situação | Status HTTP |
| --- | ---: |
| Aluno inexistente | 404 |
| DTO inválido | 400 |
| Regra de negócio | 400 |
| JSON inválido | 400 |
| Parâmetro incompatível | 400 |
| Erro inesperado | 500 |

## B) Questões

### 1.
A exceção é um mecanismo do código Java que sinaliza uma falha dentro do Service.
O 404 é um código de status do protocolo HTTP enviado na resposta. O handler faz a ponte entre a exceção e esse status.

### 2.
Sem tratamento global, o código fica duplicado e os controllers misturam fluxo normal com tratamento de erro.
Também podem surgir respostas inconsistentes, esquecimentos em endpoints e maior dificuldade de manutenção.

### 3.
`@RestControllerAdvice` centraliza o tratamento de exceções de vários controllers em uma classe.
Como é usado com controllers REST, os métodos tratados retornam respostas JSON automaticamente.

### 4.
`@ExceptionHandler` marca um método que trata um tipo específico de exceção.
O tipo informado na anotação define qual exceção será capturada pelo método.

### 5.
Uma resposta padronizada facilita o consumo pelo front-end e torna a API previsível.
Também melhora a depuração e mantém um contrato estável com uma única lógica de leitura do erro.

### 6.
A lista estruturada informa o campo e a mensagem correspondente.
Assim, o front-end consegue exibir cada erro ao lado do input correto e mostrar todos de uma vez.

### 7.
O status 400 indica que a requisição foi malformada ou inválida por causa dos dados enviados.
O status 404 indica que a requisição está correta, mas o recurso solicitado não existe.

### 8.
Exemplos são e-mail ou CPF já cadastrado, matrícula duplicada, edição concorrente e exclusão bloqueada por vínculo.
A atividade adota 400 para regras de negócio, mas 409 também seria uma escolha semanticamente válida para conflito.

### 9.
Detalhes internos podem expor classes, pacotes, bibliotecas, versões, SQL e caminhos do servidor.
Isso facilita ataques e ainda produz uma mensagem ruim para o usuário final.

### 10.
O log do servidor é um registro interno e detalhado para a equipe técnica, podendo conter stack trace.
A resposta HTTP é o que o cliente recebe e deve ser controlada, sem detalhes internos do sistema.

## C) Desafio do código

O front-end pode usar um código estável, como `ALUNO_NAO_ENCONTRADO`, para decidir ações e mensagens.
Isso permite internacionalização, redirecionamento ou abertura de uma tela específica sem depender do texto da mensagem, que pode mudar.

## D) Roteiro de testes manuais no Swagger

O projeto local usa as rotas `/api/...`. A dependência Springdoc ainda não está configurada, então o endereço abaixo ficará pendente até essa configuração ser autorizada:
`http://localhost:8080/swagger-ui.html`

1. **Atividade 29: aluno inexistente**
   - `GET /api/alunos/99999`
   - Esperado: `404`, erro `Recurso não encontrado`, código `ALUNO_NAO_ENCONTRADO` e `campos: []`.

2. **Atividade 30: matrícula ativa duplicada**
   - `POST /api/matriculas` com `{"alunoId": 1, "cursoId": 2}` para aluno e curso já matriculados.
   - Esperado: `400`, erro `Regra de negócio` e mensagem de matrícula ativa duplicada.

3. **Atividade 31: DTO inválido**
   - `POST /api/alunos` com `{"nome":"", "email":"email-invalido"}`.
   - Esperado: `400`, erro `Erro de validação` e lista com os campos `nome` e `email`.

4. **Atividade 32: parâmetro incompatível**
   - `GET /api/alunos/abc`.
   - Esperado: `400`, erro `Parâmetro inválido` e mensagem informando o parâmetro `id`.

5. **Atividade 33: status de matrícula inválido**
   - `POST /api/matriculas` com `{"alunoId": 1, "cursoId": 2, "status":"DESCONHECIDA"}`.
   - Esperado: `400`, mensagem fixa `Não foi possível interpretar os dados enviados.`, sem nome de classe ou exceção.