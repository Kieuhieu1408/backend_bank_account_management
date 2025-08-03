package com.cqrs.handler;

import com.cqrs.command.Command;

public interface CommandHandler<C extends Command> {
    void handle(C command);
}
