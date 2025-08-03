package com.cqrs.business.user.command;

import com.cqrs.command.Command;
import lombok.Getter;

@Getter
public class CreateUserCommand implements Command {

    private String username;

    public CreateUserCommand(String username) {
        this.username = username;
    }

}
