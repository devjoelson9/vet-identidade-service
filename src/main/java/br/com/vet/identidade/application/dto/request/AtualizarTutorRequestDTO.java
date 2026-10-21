package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;

public record AtualizarTutorRequestDTO(
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
        String endereco
) {
}