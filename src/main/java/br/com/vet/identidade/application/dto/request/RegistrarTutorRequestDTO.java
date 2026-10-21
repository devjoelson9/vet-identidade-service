package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record RegistrarTutorRequestDTO(
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
        String cpf,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String senha,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
        String endereco
) {
    public String getCpfLimpo() {
        return cpf != null ? cpf.trim() : null;
    }

    public String getEmailNormalizado() {
        return email != null ? email.trim().toLowerCase() : null;
    }
}