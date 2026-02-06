package com.pitisha.project.mybank.notificationservice.infrastructure.messaging;

import com.pitisha.project.mybank.kafka.event.AccountCreditedEvent;
import com.pitisha.project.mybank.kafka.event.AccountWithdrawnEvent;
import com.pitisha.project.mybank.notificationservice.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.pitisha.project.mybank.notificationservice.domain.entity.NotificationType.CREDITED;
import static com.pitisha.project.mybank.notificationservice.domain.entity.NotificationType.WITHDRAWN;
import static java.math.RoundingMode.HALF_UP;

@Component
@KafkaListener(topics = "account-operations-topic")
@Slf4j
@RequiredArgsConstructor
public class AccountOperationEventsHandler {

    private static final String WITHDRAWN_TEMPLATE = "You sent %s %s from %s";
    private static final String CREDITED_TEMPLATE = "You received %s %s to %s";
    private static final String RECEIVED_WITHDRAWN_MESSAGE = "Received account withdrawn event: {}";
    private static final String RECEIVED_CREDITED_MESSAGE = "Received account credited event: {}";

    private final NotificationService notificationService;

    @KafkaHandler
    public void handleAccountWithdrawnEvent(@Payload final AccountWithdrawnEvent event) {
        log.info(RECEIVED_WITHDRAWN_MESSAGE, event);
        notificationService.add(
            event.transactionId(),
            WITHDRAWN,
            event.ownerId(),
            WITHDRAWN_TEMPLATE.formatted(event.amount().setScale(2, HALF_UP), event.currency().name(), event.accountId())
        );
    }

    @KafkaHandler
    public void handleAccountCreditedEvent(@Payload final AccountCreditedEvent event) {
        log.info(RECEIVED_CREDITED_MESSAGE, event);
        notificationService.add(
            event.transactionId(),
            CREDITED,
            event.ownerId(),
            CREDITED_TEMPLATE.formatted(event.amount().setScale(2, HALF_UP), event.currency().name(), event.accountId())
        );
    }
}
