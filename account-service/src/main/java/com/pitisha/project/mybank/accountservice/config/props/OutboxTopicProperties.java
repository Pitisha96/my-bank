package com.pitisha.project.mybank.accountservice.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "outbox")
@Getter
@Setter
public class OutboxTopicProperties {
    private Map<String, String> topics;
}
