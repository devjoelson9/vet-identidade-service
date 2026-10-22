-- =============================================================================
-- V2__create_tokens_revogados.sql - Revogacao de tokens JWT (logout)
-- =============================================================================

CREATE TABLE tokens_revogados (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(36) NOT NULL UNIQUE,
    expira_em TIMESTAMP NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tokens_revogados_expira_em ON tokens_revogados (expira_em);
