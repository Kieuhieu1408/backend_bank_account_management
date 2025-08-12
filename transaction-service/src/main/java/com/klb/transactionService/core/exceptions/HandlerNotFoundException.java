package com.klb.transactionService.core.exceptions;

// Exception classes
public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException(String message) {
        super(message);
    }
}
