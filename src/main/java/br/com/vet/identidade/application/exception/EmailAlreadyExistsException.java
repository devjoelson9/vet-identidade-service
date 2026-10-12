package br.com.vet.identidade.application.exception;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}