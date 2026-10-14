package br.com.vet.identidade.application.dto.response;

import br.com.vet.identidade.domain.enums.Role;
import java.time.LocalDate;
import java.util.Set;

public record UsuarioInternoResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String status,
        Set<Role> roles,
        String crmv,
        String especialidade
) {
}