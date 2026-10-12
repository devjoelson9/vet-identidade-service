package br.com.vet.identidade.application.dto.response;

public record LoginResponse(
        String token,
        String refreshToken,
        UserDto user
) {
}