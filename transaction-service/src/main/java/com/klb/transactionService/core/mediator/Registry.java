package com.klb.transactionService.core.mediator;

import com.klb.transactionService.core.abstractions.Handler;
import com.klb.transactionService.core.abstractions.RequestData;
import com.klb.transactionService.core.abstractions.ResponseData;
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

        // Log chi tiết về các beans được tìm thấy
        handlerBeans.forEach((name, handler) -> {
            log.info("Found handler bean: {} of type {}", name, handler.getClass().getName());
        });

        handlerBeans.values().forEach(this::registerHandler);

        log.info("Registration completed. Total handlers: {}", handlers.size());
        handlers.forEach((key, value) ->
                log.info("  {} -> {}", key.getSimpleName(), value.getClass().getSimpleName())
        );
    }

    @SuppressWarnings("unchecked")
    private void registerHandler(Handler<?, ?> handler) {
        Class<?> handlerClass = handler.getClass();
        log.info("Attempting to register handler: {}", handlerClass.getName());

        // Kiểm tra nếu là Spring proxy class
        boolean isProxyClass = handlerClass.getName().contains("$$");

        // Nếu là proxy class, thử lấy superclass để tìm generic type information
        if (isProxyClass) {
            log.info("Detected proxy class, will also check superclass: {}", handlerClass.getSuperclass().getName());
        }

        // Danh sách class cần kiểm tra
        Class<?>[] classesToCheck = isProxyClass ?
                new Class<?>[]{handlerClass, handlerClass.getSuperclass()} :
                new Class<?>[]{handlerClass};

        // Kiểm tra từng class để tìm Handler interface
        boolean registered = false;
        for (Class<?> clazz : classesToCheck) {
            if (registered) break;

            // Tạo một biến final để sử dụng trong lambda
            final boolean[] wasRegistered = {false};

            // Tìm Handler interface với generic types
            Arrays.stream(clazz.getGenericInterfaces())
                    .filter(type -> type instanceof ParameterizedType)
                    .map(type -> (ParameterizedType) type)
                    .filter(paramType -> {
                        Type rawType = paramType.getRawType();
                        log.info("Checking interface: {} with raw type: {}", paramType, rawType);
                        return rawType instanceof Class &&
                                (Handler.class.equals(rawType) ||
                                        (rawType instanceof Class && Handler.class.isAssignableFrom((Class<?>) rawType)));
                    })
                    .findFirst()
                    .ifPresent(paramType -> {
                        try {
                            Type[] typeArguments = paramType.getActualTypeArguments();
                            log.info("Handler interface found with {} type arguments", typeArguments.length);

                            if (typeArguments.length >= 1) {
                                Type requestType = typeArguments[0];
                                log.info("Request type: {}", requestType);

                                if (requestType instanceof Class &&
                                        RequestData.class.isAssignableFrom((Class<?>) requestType)) {
                                    Class<? extends RequestData> requestClass = (Class<? extends RequestData>) requestType;
                                    handlers.put(requestClass, handler);
                                    log.info("Successfully registered handler for: {}", requestClass.getName());
                                    wasRegistered[0] = true;
                                } else {
                                    log.warn("Request type is not a valid RequestData class: {}", requestType);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error registering handler: {}", e.getMessage(), e);
                        }
                    });

            if (wasRegistered[0]) {
                registered = true;
            }
        }

        if (!registered) {
            log.warn("No suitable Handler interface found for handler: {}", handlerClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RequestData, R extends ResponseData> Handler<T, R> getHandler(Class<T> requestClass) {
        return (Handler<T, R>) handlers.get(requestClass);
    }

    public boolean hasHandler(Class<? extends RequestData> requestClass) {
        return handlers.containsKey(requestClass);
    }
}
