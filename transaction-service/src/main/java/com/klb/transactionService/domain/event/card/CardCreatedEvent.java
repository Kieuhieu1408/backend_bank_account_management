package com.klb.transactionService.domain.event.card;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class CardCreatedEvent extends ApplicationEvent {
    public CardCreatedEvent(Object source) {
        super(source);
    }

    public CardCreatedEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
