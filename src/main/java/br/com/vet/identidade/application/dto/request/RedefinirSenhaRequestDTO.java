package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;

public record RedefinirSenhaRequestDTO(
        @NotBlank(message = "Token é obrigatório")
        String token,

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, max = 100, message = "Nova senha deve ter entre 8 e 100 caracteres")
        String novaSenha,

        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmarSenha
) {
    public boolean senhasConferem() {
        return novaSenha != null && novaSenha.equals(confirmarSenha);
    }
}