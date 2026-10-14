package br.com.vet.identidade.application.exception;

public class CrmvAlreadyExistsException extends BusinessException {
    public CrmvAlreadyExistsException(String crmv) {
        super("CRMV já cadastrado: " + crmv);
    }
}