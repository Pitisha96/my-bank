package com.pitisha.project.mybank.accountservice.infrastructure.outbox;

import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;

import com.pitisha.project.mybank.accountservice.config.props.OutboxTopicProperties;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountsOutboxRepository;
import com.pitisha.project.mybank.kafka.event.AccountKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountsOutboxHandler {

    private static final String CLASS_NOT_FOUND = "Class not found for topic name: %s";
    private static final String UNKNOWN_TOPIC = "Unknown topic: %s";
    private static final String FAILED_TO_SEND_KAFKA_EVENT = "Failed to send kafka event";
    private static final String KAFKA_EVENT_SENT = "Kafka event sent for account = {}";
    private static final String SCHEDULER_WAS_INTERRUPTED = "Scheduler was interrupted";

    private final KafkaTemplate<String, AccountKafkaEvent> kafkaTemplate;
    private final AccountsOutboxRepository outboxRepository;
    private final OutboxTopicProperties topicProperties;
    private final JsonMapper jsonMapper;

    private final Map<String, Class<? extends AccountKafkaEvent>> cache = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void handle() {
        final var outboxes = outboxRepository.lockBatch(100);
        for (final var outbox : outboxes) {
            try {
                final AccountKafkaEvent event = getAccountKafkaEvent(outbox.getTopic(), outbox.getPayload());
                kafkaTemplate
                        .send(outbox.getTopic(), event.accountId().toString(), event)
                        .get();
                log.info(KAFKA_EVENT_SENT, event.accountId());
                outboxRepository.delete(outbox);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                log.error(SCHEDULER_WAS_INTERRUPTED, e);
                throw new RuntimeException(SCHEDULER_WAS_INTERRUPTED, e);
            } catch (Exception e) {
                log.error(FAILED_TO_SEND_KAFKA_EVENT, e);
                throw new RuntimeException(e);
            }
        }
    }

    private AccountKafkaEvent getAccountKafkaEvent(final String topic, final String payload) {
        return jsonMapper.readValue(payload, getClassByTopicName(topic));
    }

    private Class<? extends AccountKafkaEvent> getClassByTopicName(final String topicName) {
        return cache.computeIfAbsent(topicName, t -> {
            final String className = topicProperties.getTopics().get(topicName);
            if (isNull(className)) {
                throw new IllegalStateException(UNKNOWN_TOPIC.formatted(topicName));
            }
            try {
                return (Class<? extends AccountKafkaEvent>) forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(CLASS_NOT_FOUND.formatted(topicName));
            }
        });
    }
}
