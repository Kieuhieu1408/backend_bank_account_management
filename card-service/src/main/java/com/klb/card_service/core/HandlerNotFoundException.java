package com.klb.card_service.core;

// Exception classes
public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException(String message) {
        super(message);
    }
}
