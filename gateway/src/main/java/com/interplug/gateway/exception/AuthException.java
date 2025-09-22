package com.interplug.gateway.exception;

public class AuthException extends RuntimeException {

    private final ErrorType errorType;

    public AuthException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
