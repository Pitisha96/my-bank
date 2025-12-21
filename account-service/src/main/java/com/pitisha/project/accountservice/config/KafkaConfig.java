package com.pitisha.project.accountservice.config;

import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREATED_TOPIC;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.pitisha.project.mybank.kafka.event.AccountCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9092,localhost:9093}")
    private String bootstrapServers;

    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name(ACCOUNT_CREATED_TOPIC.getTopicName())
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    public KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate(final ProducerFactory<String, AccountCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, AccountCreatedEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    public Map<String, Object> producerConfigs() {
        final Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configs.put(ACKS_CONFIG, "all");
        configs.put(DELIVERY_TIMEOUT_MS_CONFIG, 10000);
        configs.put(LINGER_MS_CONFIG, 0);
        configs.put(REQUEST_TIMEOUT_MS_CONFIG, 5000);
        configs.put(ENABLE_IDEMPOTENCE_CONFIG, true);
        return configs;
    }
}
