package com.aegis.backend.exception;

public class AegisException extends RuntimeException {
    
    public AegisException(String message) {
        super(message);
    }

    public AegisException(String message, Throwable cause) {
        super(message, cause);
    }
}
