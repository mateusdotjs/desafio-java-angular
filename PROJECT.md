# PROVA FULLSTACK JAVA

## Objetivo

Desenvolver uma API REST completa utilizando Spring Boot, JPA e PostgreSQL,
consumindo um serviço externo de CEP. Além disso, desenvolver um frontend que
consuma essa API.

## Requisitos da API

### Gerenciamento de Usuários e Endereços

1. Criar um CRUD completo de usuários e um CRUD de endereços.
   - Um usuário pode ter múltiplos endereços.
   - O relacionamento deve ser unidirecional ou bidirecional, conforme
     necessidade.
   - O usuário deve ter os campos: ID, Nome, Email (único), Senha (criptografada).
   - O endereço deve conter: ID, Logradouro, Número, Complemento, Bairro,
     Cidade, Estado, CEP, Usuário associado.

### Autenticação e Autorização

2. Implementar Spring Security e autenticação via JWT.
   - Criar níveis de acesso:
     - Admin: Pode gerenciar todos os usuários.
     - Usuário comum: Só pode acessar e modificar seus próprios dados.

### Consumo de Serviço Externo

3. Implementar consumo do serviço de CEP:
   ```sh
   curl https://viacep.com.br/ws/{cep}/json/
   ```
   - O CEP informado deve ser validado e armazenado corretamente no banco de
     dados.

### Paginação e Ordenação

4. Os endpoints de listagem devem suportar paginação e ordenar por nome,
   email e data de criação.

### Tratamento de Exceções

5. Implementar Global Exception Handler para capturar erros de validação,
   autenticação e requisições inválidas.

### Testes Unitários e de Integração

6. Criar testes unitários para serviços e repositórios usando JUnit e Mockito.
   - Criar testes de integração para os endpoints principais.

## Requisitos do Frontend

Desenvolver um frontend SPA (Single Page Application) utilizando uma das
tecnologias abaixo:
- Angular
- Thymeleaf
- JSP

O frontend deve conter:
- Tela de Login e Registro
- Dashboard com listagem de usuários e endereços
- Formulários para cadastro e edição
- Consumo da API REST
- Feedback visual (toasts, mensagens de erro, loaders)

## Entrega Final

- Código fonte no GitHub ou GitLab.
- Instruções de instalação e execução no README.md.
- Demonstração em vídeo (opcional) mostrando as principais funcionalidades.
