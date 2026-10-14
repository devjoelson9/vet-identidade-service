package br.com.vet.identidade.application.exception;

public class InvalidRoleException extends BusinessException {
    public InvalidRoleException(String role) {
        super("Perfil inválido para usuário interno: " + role + ". Permitidos: RECEPCIONISTA, VETERINARIO");
    }
}