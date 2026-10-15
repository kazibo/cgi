package com.example.cgi.exception;

public class ExternalLoggingException extends RuntimeException {
    public ExternalLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}