package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;

public record SolicitarRecuperacaoSenhaRequestDTO(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {
}