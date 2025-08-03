package com.cqrs.mediator;

import com.cqrs.command.Command;
import com.cqrs.query.Query;

public interface Mediator {
    <C extends Command> void send(C command);

    <Q extends Query<R>, R> R send(Q query);
}
