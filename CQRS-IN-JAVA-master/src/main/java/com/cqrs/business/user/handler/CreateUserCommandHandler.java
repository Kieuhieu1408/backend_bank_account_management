package com.cqrs.business.user.handler;

import com.cqrs.business.user.command.CreateUserCommand;
import com.cqrs.handler.CommandHandler;

public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand> {
    @Override
    public void handle(CreateUserCommand command) {
        System.out.println("User created: " + command.getUsername());
    }

}
