package com.cqrs.mediator;

import com.cqrs.command.Command;
import com.cqrs.handler.CommandHandler;
import com.cqrs.handler.QueryHandler;
import com.cqrs.query.Query;

import java.util.HashMap;
import java.util.Map;

public class ConcreteMediator implements Mediator {

    private final Map<Class<?>, CommandHandler<?>> commandHandlers = new HashMap<>();

    private final Map<Class<?>, QueryHandler<?, ?>> queryHandlers = new HashMap<>();

    @Override
    public <C extends Command> void send(C command) {
        CommandHandler<C> handler = (CommandHandler<C>) commandHandlers.get(command.getClass());
        if (handler != null) {
            handler.handle(command);
        } else {
            throw new UnsupportedOperationException("No handler registered for command: " + command.getClass());
        }
    }

    @Override
    public <Q extends Query<R>, R> R send(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) queryHandlers.get(query.getClass());
        if (handler != null) {
            return handler.handle(query);
        } else {
            throw new UnsupportedOperationException("No handler registered for query: " + query.getClass());
        }
    }

    public <C extends Command> void registerCommandHandler(Class<C> commandClass, CommandHandler<C> handler) {
        commandHandlers.put(commandClass, handler);
    }

    public <Q extends Query<R>, R> void registerQueryHandler(Class<Q> queryClass, QueryHandler<Q, R> handler) {
        queryHandlers.put(queryClass, handler);
    }

}
