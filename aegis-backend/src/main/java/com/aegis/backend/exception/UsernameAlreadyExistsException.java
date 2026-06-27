package com.aegis.backend.exception;

public class UsernameAlreadyExistsException extends AuthException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
