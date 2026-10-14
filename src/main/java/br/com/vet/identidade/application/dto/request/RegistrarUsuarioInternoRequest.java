package br.com.vet.identidade.application.dto.request;

import br.com.vet.identidade.domain.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;

public record RegistrarUsuarioInternoRequest(
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String senha,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotNull(message = "Perfil é obrigatório")
        Role role,

        @Size(max = 20, message = "CRMV deve ter no máximo 20 caracteres")
        String crmv,

        @Size(max = 100, message = "Especialidade deve ter no máximo 100 caracteres")
        String especialidade
) {
    @AssertTrue(message = "CRMV e especialidade são obrigatórios para VETERINARIO")
    public boolean isCrmvValido() {
        return role != Role.VETERINARIO 
            || (crmv != null && !crmv.isBlank() && especialidade != null && !especialidade.isBlank());
    }

    @AssertTrue(message = "Perfil deve ser RECEPCIONISTA ou VETERINARIO")
    public boolean isRoleValida() {
        return role == Role.RECEPCIONISTA || role == Role.VETERINARIO;
    }

    public String getCpfLimpo() {
        return cpf != null ? cpf.trim() : null;
    }

    public String getEmailNormalizado() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public String getCrmvNormalizado() {
        return crmv != null ? crmv.trim().toUpperCase() : null;
    }
}