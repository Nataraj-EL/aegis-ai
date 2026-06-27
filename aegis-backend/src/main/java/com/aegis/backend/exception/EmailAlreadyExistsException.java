package com.aegis.backend.exception;

public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
