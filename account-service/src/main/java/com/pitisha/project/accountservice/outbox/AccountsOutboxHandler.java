package com.pitisha.project.accountservice.outbox;

import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREATED_TOPIC;
import static java.util.Objects.nonNull;

import com.pitisha.project.accountservice.domain.repository.AccountsOutboxRepository;
import com.pitisha.project.mybank.kafka.event.AccountCreatedEvent;
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

    private static final String FAILED_TO_SEND_KAFKA_EVENT = "Failed to send kafka event: {}";
    private static final String KAFKA_EVENT_SENT = "Kafka event sent: {}, metadata: {}";

    private final AccountsOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;
    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional(readOnly = true)
    public void handle() {
        final var outboxes = outboxRepository.findByProcessedFalse();
        outboxes.forEach(outbox -> {
            final var future = kafkaTemplate.send(
                    ACCOUNT_CREATED_TOPIC.getTopicName(),
                    jsonMapper.readValue(outbox.getPayload(), AccountCreatedEvent.class)
            );
            future.whenComplete((result, error) -> {
                if (nonNull(error)) {
                    log.error(FAILED_TO_SEND_KAFKA_EVENT, error.getMessage(), error);
                    return;
                }
                outbox.setProcessed(true);
                outboxRepository.save(outbox);
                log.info(KAFKA_EVENT_SENT, result.getProducerRecord().value(), result.getRecordMetadata());
            });
        });
    }
}
