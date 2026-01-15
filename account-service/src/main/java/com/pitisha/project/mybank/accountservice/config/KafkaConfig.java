package com.pitisha.project.mybank.accountservice.config;

import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREATED_TOPIC;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREDITED_TOPIC;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_WITHDRAWN_TOPIC;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.config.TopicBuilder.name;

import com.pitisha.project.mybank.accountservice.config.props.OutboxTopicProperties;
import com.pitisha.project.mybank.kafka.event.AccountKafkaEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(OutboxTopicProperties.class)
public class KafkaConfig {

    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9092,localhost:9093}")
    private String bootstrapServers;

    @Bean
    public NewTopic createAccountCreatedTopic() {
        return name(ACCOUNT_CREATED_TOPIC.getTopicName())
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    public NewTopic createAccountCreditedTopic() {
        return name(ACCOUNT_CREDITED_TOPIC.getTopicName())
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    public NewTopic createAccountWithdrawnTopic() {
        return name(ACCOUNT_WITHDRAWN_TOPIC.getTopicName())
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    public KafkaTemplate<String, AccountKafkaEvent> kafkaTemplate(final ProducerFactory<String, AccountKafkaEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, AccountKafkaEvent> producerFactory() {
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
