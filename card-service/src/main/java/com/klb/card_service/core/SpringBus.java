package com.klb.card_service.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringBus implements CqrsBus {

    private final Registry registry;

    public SpringBus(Registry registry) {
        this.registry = registry;
    }

    @Override
    public <T extends RequestData, R extends ResponseData> R execute(T requestData) {
        Class<T> requestClass = (Class<T>) requestData.getClass();

        log.debug("Executing: {}", requestClass.getSimpleName());

        Handler<T, R> handler = registry.getHandler(requestClass);

        if (handler == null) {
            log.error("No handler found for: {}", requestClass.getSimpleName());
            throw new HandlerNotFoundException("No handler registered for: " + requestClass.getSimpleName());
        }

        try {
            R response = handler.handle(requestData);
            log.debug("Executed successfully: {}", requestClass.getSimpleName());
            return response;

        } catch (Exception e) {
            log.error("Error executing {}: {}", requestClass.getSimpleName(), e.getMessage(), e);
            throw new HandlerExecutionException("Error executing " + requestClass.getSimpleName(), e);
        }
    }
}

