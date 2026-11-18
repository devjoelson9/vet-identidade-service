package br.com.vet.identidade.application.dto.response;

public record VeterinarioResumoResponse(
        Long id,
        String nomeCompleto,
        String crmv,
        String especialidade,
        String status
) {
}
