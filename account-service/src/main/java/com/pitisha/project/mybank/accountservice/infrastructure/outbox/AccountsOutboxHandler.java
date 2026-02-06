package com.pitisha.project.mybank.accountservice.infrastructure.outbox;

import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountsOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountsOutboxHandler {

    private static final String CLASS_NOT_FOUND = "Class not found for name: %s";
    private static final String FAILED_TO_SEND_KAFKA_EVENT = "Failed to send kafka event";
    private static final String KAFKA_EVENT_SENT = "Kafka event sent {}";
    private static final String SCHEDULER_WAS_INTERRUPTED = "Scheduler was interrupted";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AccountsOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void handle() {
        final var outboxes = outboxRepository.lockBatch(100);
        for (final var outbox : outboxes) {
            try {
                final Object event = getKafkaEvent(outbox);
                kafkaTemplate
                        .send(outbox.getTopic(), outbox.getKey(), event)
                        .get();
                log.info(KAFKA_EVENT_SENT, event);
                outboxRepository.delete(outbox);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                log.error(SCHEDULER_WAS_INTERRUPTED, e);
                throw new IllegalStateException(SCHEDULER_WAS_INTERRUPTED, e);
            } catch (Exception e) {
                log.error(FAILED_TO_SEND_KAFKA_EVENT, e);
                throw new RuntimeException(e);
            }
        }
    }

    private Object getKafkaEvent(final AccountOutboxEntity outbox) {
        try {
            return jsonMapper.readValue(outbox.getPayload(), forName(outbox.getPayloadType()));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(CLASS_NOT_FOUND.formatted(outbox.getPayloadType()));
        }
    }
}
