package com.pokedex.exception;

import org.springframework.http.HttpStatus;

/** Violação de regra de negócio (ID/nome duplicado, tipo inválido...). */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
