package com.example.backend.exception;

public class SmlBuildingException extends Exception {
    
    public SmlBuildingException(String message) {
        super(message);
    }
    
    public SmlBuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
