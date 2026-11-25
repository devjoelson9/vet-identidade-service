# Serviço de Identidade — Sistema de Gestão Veterinária

Microsserviço responsável por autenticação e usuários: tutores, animais, usuários internos (administrador, recepção e veterinário), emissão de tokens JWT e recuperação de senha por e-mail.

## Tecnologias

- Java 21 e Spring Boot 4.1.1
- Spring Web MVC, Spring Data JPA, Spring Security (JWT) e Bean Validation
- PostgreSQL com migrações Flyway
- Spring Mail (Mailpit local ou SMTP)
- springdoc-openapi (Swagger UI)

## Rotas principais

| Recurso | Prefixo |
|---|---|
| Login, renovação e logout | `/api/v1/auth` |
| Recuperação e definição de senha | `/api/v1/auth` |
| Perfil e animais do tutor autenticado | `/api/v1/tutores/me` |
| Tutores (administração) | `/api/v1/admin/tutores` |
| Usuários internos (administração) | `/api/v1/admin/usuarios` |
| Tutores e animais (recepção) | `/api/v1/recepcionista/tutores` |
| Animais sob responsabilidade do veterinário | `/api/v1/veterinario/animais` |
| Veterinários | `/api/v1/veterinarios` |

As rotas exigem o header `Authorization: Bearer <token>`, exceto login, recuperação de senha e a documentação.

Documentação interativa: `http://localhost:8081/swagger-ui.html`.

## Integrações

Este serviço assina os tokens JWT utilizados por todos os demais microsserviços. Clínico, agendamento e faturamento validam a assinatura com o **mesmo** `JWT_SECRET`.

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` | Conexão com o PostgreSQL |
| `JWT_SECRET` | Segredo compartilhado entre os serviços (mínimo de 256 bits) |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS`, `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Validade dos tokens |
| `CORS_ALLOWED_ORIGIN` | Origem liberada para o frontend |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`, `MAIL_FROM` | Envio de e-mail |
| `FRONTEND_URL` | Base usada nos links enviados por e-mail |

## Como executar

Ambiente completo (recomendado), a partir de `vet-infrastructure`:

```bash
docker compose up -d --build servico-identidade
```

Execução local, com um PostgreSQL disponível:

```bash
./mvnw spring-boot:run
```

Testes:

```bash
./mvnw test
```

## Estrutura

```text
src/main/java/br/com/vet/identidade/
├── application/     # serviços de caso de uso, mappers e DTOs
├── domain/          # entidades, enums e repositórios
├── infrastructure/  # segurança, e-mail, integrações e configuração
└── web/             # controllers e tratamento de exceções
```
