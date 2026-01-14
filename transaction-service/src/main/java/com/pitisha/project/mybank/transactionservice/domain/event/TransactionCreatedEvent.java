package com.pitisha.project.mybank.transactionservice.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TransactionCreatedEvent extends ApplicationEvent {

    private final UUID transactionId;

    public TransactionCreatedEvent(final Object source, final UUID transactionId) {
        super(source);
        this.transactionId = transactionId;
    }
}
