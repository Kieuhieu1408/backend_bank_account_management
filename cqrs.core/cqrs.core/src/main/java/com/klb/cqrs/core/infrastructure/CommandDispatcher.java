package com.klb.cqrs.core.infrastructure;

import com.klb.cqrs.core.commands.BaseCommand;
import com.klb.cqrs.core.commands.CommandHandlerMethod;

// Mediator
public interface CommandDispatcher {
    <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> handler);
    void send(BaseCommand command);
}
