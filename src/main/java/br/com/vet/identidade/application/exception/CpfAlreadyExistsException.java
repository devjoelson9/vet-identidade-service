package br.com.vet.identidade.application.exception;

public class CpfAlreadyExistsException extends BusinessException {
    public CpfAlreadyExistsException(String cpf) {
        super("CPF já cadastrado: " + cpf);
    }
}