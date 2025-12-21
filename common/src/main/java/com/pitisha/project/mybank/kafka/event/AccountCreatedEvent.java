package com.pitisha.project.mybank.kafka.event;

import java.util.UUID;

public record AccountCreatedEvent(
        UUID number,
        UUID ownerId,
        String currency
) { }
