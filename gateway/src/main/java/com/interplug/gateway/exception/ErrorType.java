package com.interplug.gateway.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;

public enum ErrorType {
    TOKEN_AUTHORIZATION_FAIL(HttpStatus.UNAUTHORIZED, "토큰 인증에 실패했습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");

    private final HttpStatusCode statusCode;
    private final String message;

    ErrorType(HttpStatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}