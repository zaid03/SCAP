package com.example.backend.exception;

public class SmlProcessingException extends RuntimeException {
    
    public SmlProcessingException(String message) {
        super(message);
    }
    
    public SmlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SmlProcessingException(Throwable cause) {
        super(cause);
    }
}
