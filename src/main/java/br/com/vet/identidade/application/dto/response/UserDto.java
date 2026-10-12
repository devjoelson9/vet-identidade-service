package br.com.vet.identidade.application.dto.response;

import br.com.vet.identidade.domain.enums.Role;

import java.util.Set;

public record UserDto(
        Long id,
        String nomeCompleto,
        String email,
        Set<Role> roles,
        String status
) {
}