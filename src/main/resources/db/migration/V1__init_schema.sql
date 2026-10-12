-- =============================================================================
-- V1__init_schema.sql - Módulo de Identidade e Cadastro (vet-identidade-service)
-- =============================================================================

-- 1. Tabela de Usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    data_nascimento DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP,
    deletado_em TIMESTAMP -- Indica a data/hora da exclusão lógica (NULL se ativo)
);

-- Garantir Unicidade de CPF e E-mail APENAS para usuários NÃO deletados
CREATE UNIQUE INDEX idx_usuarios_cpf_ativo ON usuarios (cpf) WHERE deletado_em IS NULL;
CREATE UNIQUE INDEX idx_usuarios_email_ativo ON usuarios (email) WHERE deletado_em IS NULL;


-- 2. Tabela de Roles/Perfis do Usuário
CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT pk_usuario_roles PRIMARY KEY (usuario_id, role)
);


-- 3. Tabela de Tutores
CREATE TABLE tutores (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    endereco TEXT,
    deletado_em TIMESTAMP,
    CONSTRAINT fk_tutor_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);


-- 4. Tabela de Veterinários
CREATE TABLE veterinarios (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    crmv VARCHAR(20) NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    deletado_em TIMESTAMP,
    CONSTRAINT fk_veterinario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Garantir Unicidade de CRMV APENAS para veterinários NÃO deletados
CREATE UNIQUE INDEX idx_veterinarios_crmv_ativo ON veterinarios (crmv) WHERE deletado_em IS NULL;


-- 5. Tabela de Animais
CREATE TABLE animais (
     id BIGSERIAL PRIMARY KEY,
     tutor_id BIGINT NOT NULL,
     nome VARCHAR(100) NOT NULL,
     especie VARCHAR(50) NOT NULL,
     raca VARCHAR(50),
     sexo VARCHAR(10) NOT NULL,
     data_nascimento DATE,
     peso DECIMAL(5, 2),
     observacoes TEXT,
     criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     atualizado_em TIMESTAMP,
     deletado_em TIMESTAMP,
     CONSTRAINT fk_animal_tutor FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
       id BIGSERIAL PRIMARY KEY,
       token VARCHAR(255) NOT NULL UNIQUE,
       usuario_id BIGINT NOT NULL,
       data_expiracao TIMESTAMP NOT NULL,
       usado BOOLEAN NOT NULL DEFAULT FALSE,
       criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_password_reset_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

INSERT INTO usuarios (nome_completo, cpf, email, senha_hash, status)
VALUES ('Administrador do Sistema', '00000000000', 'admin@maisvet.com', '$2a$10$e846D4aL0Rk6E6e.HwH8vef156d9jZp7i88I4729f6A220d/Z9/iG', 'ATIVO');

INSERT INTO usuario_roles (usuario_id, role)
VALUES (1, 'ADMIN');