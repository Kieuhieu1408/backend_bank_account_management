package com.klb.card_service.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class Registry {

    private final Map<Class<? extends RequestData>, Handler<?, ?>> handlers = new ConcurrentHashMap<>();

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, Handler> handlerBeans = context.getBeansOfType(Handler.class);

        log.info("Registering {} handlers", handlerBeans.size());

        handlerBeans.values().forEach(this::registerHandler);

        log.info("Registration completed. Total handlers: {}", handlers.size());
        handlers.forEach((key, value) ->
                log.info("  {} -> {}", key.getSimpleName(), value.getClass().getSimpleName())
        );
    }

    @SuppressWarnings("unchecked")
    private void registerHandler(Handler<?, ?> handler) {
        Class<?> handlerClass = handler.getClass();

        // Find Handler interface with generic types
        Arrays.stream(handlerClass.getGenericInterfaces())
                .filter(type -> type instanceof ParameterizedType)
                .map(type -> (ParameterizedType) type)
                .filter(paramType -> Handler.class.equals(paramType.getRawType()))
                .findFirst()
                .ifPresent(paramType -> {
                    Type requestType = paramType.getActualTypeArguments()[0];
                    if (requestType instanceof Class) {
                        Class<? extends RequestData> requestClass = (Class<? extends RequestData>) requestType;
                        handlers.put(requestClass, handler);
                        log.debug("Registered: {} -> {}", requestClass.getSimpleName(), handlerClass.getSimpleName());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public <T extends RequestData, R extends ResponseData> Handler<T, R> getHandler(Class<T> requestClass) {
        return (Handler<T, R>) handlers.get(requestClass);
    }

    public boolean hasHandler(Class<? extends RequestData> requestClass) {
        return handlers.containsKey(requestClass);
    }
}
