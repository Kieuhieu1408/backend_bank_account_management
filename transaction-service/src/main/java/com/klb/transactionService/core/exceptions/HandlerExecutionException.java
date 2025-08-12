package com.klb.transactionService.core.exceptions;

public class HandlerExecutionException extends RuntimeException {
    public HandlerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
