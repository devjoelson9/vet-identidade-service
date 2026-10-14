package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AtualizarUsuarioInternoRequest(
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 20, message = "CRMV deve ter no máximo 20 caracteres")
        String crmv,

        @Size(max = 100, message = "Especialidade deve ter no máximo 100 caracteres")
        String especialidade
) {
    public String getCrmvNormalizado() {
        return crmv != null ? crmv.trim().toUpperCase() : null;
    }
}