# Desafio Java Angular

Aplicação fullstack para gerenciamento de usuários e endereços.

O projeto possui uma API REST em Spring Boot, um frontend em Angular e um banco
PostgreSQL. A autenticação é feita com JWT, com perfis de acesso para
administrador e usuário comum.

## Como executar

### Pré-requisitos

- Java 17
- Node.js e npm
- Docker e Docker Compose, caso queira subir o banco em container

### 1. Subir o banco de dados

Com Docker, na raiz do projeto, execute:

```bash
docker compose up -d
```

Esse comando inicia o PostgreSQL na porta `5432`, usando o banco `user_api`.

Se não tiver Docker, também é possível usar um PostgreSQL local. Nesse caso,
crie um banco chamado `user_api` e configure um usuário com as mesmas
credenciais esperadas pela aplicação:

```txt
banco: user_api
usuário: postgres
senha: postgres
porta: 5432
```

Esses valores estão configurados em `backend/src/main/resources/application.properties`.

### 2. Iniciar o backend

Em outro terminal, execute:

```bash
cd backend
./mvnw spring-boot:run
```

A API ficará disponível em:

```txt
http://localhost:8080
```

Ao iniciar, a aplicação cria um usuário administrador:

```txt
email: admin@email.com
senha: admin123
```

### 3. Iniciar o frontend

Em outro terminal, execute:

```bash
cd frontend
npm install
npm start
```

O frontend ficará disponível em:

```txt
http://localhost:4200
```

## Funcionalidades

- Login e cadastro de usuários.
- Autenticação com JWT.
- Controle de acesso por perfil.
- Administrador pode gerenciar usuários.
- Usuário comum pode acessar e alterar seus próprios dados.
- CRUD de endereços.
- Consulta de CEP pela API do ViaCEP.
- Paginação e ordenação nas listagens.
- Tratamento padronizado de erros.
- Feedback visual no frontend.

## Tecnologias utilizadas

### Backend

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- JUnit
- Mockito
- MockMvc
- Testcontainers

### Frontend

- Angular
- TypeScript
- Angular Router
- Reactive Forms
- HttpClient
- Angular Material
- RxJS
- Vitest

## Estrutura do projeto

```txt
backend/
  src/main/java/com/example/backend/
    auth/       autenticação e registro
    user/       usuários
    address/    endereços
    zipcode/    integração com ViaCEP
    security/   JWT, filtros e autorização
    common/     erros, respostas e paginação

frontend/
  src/app/
    core/       autenticação, guards, interceptors e serviços base
    features/   telas e funcionalidades principais
    layout/     estrutura visual da aplicação
    shared/     componentes compartilhados
```

## Testes

Para executar os testes do backend:

```bash
cd backend
./mvnw test
```

Para executar os testes do frontend:

```bash
cd frontend
npm test
```

Os testes do backend incluem testes unitários e testes de integração com
MockMvc e Testcontainers. No frontend, os testes cobrem componentes, services,
guards e interceptors.

## Observação sobre execução

Neste projeto, apenas o banco de dados roda no Docker Compose. O backend e o
frontend são iniciados separadamente para facilitar o desenvolvimento, o debug
e o hot reload durante alterações no código.

## Conta de administrador

A única conta com perfil de administrador é a criada automaticamente quando o
backend inicia pela primeira vez:

```txt
email: admin@email.com
senha: admin123
```

O cadastro pela tela de registro e a criação de usuários pelo administrador
sempre geram contas com perfil de usuário comum (`USER`). A aplicação não
oferece uma forma de promover outra conta a administrador.

Para ter outro administrador, altere o perfil direto no banco, na coluna
`role` da tabela `users`. Os valores aceitos são `ADMIN` e `USER`:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'outro@email.com';
```

A conta precisa já existir. Depois da alteração, o novo perfil vale no próximo
login.
