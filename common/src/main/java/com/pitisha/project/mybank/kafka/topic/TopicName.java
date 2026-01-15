package com.pitisha.project.mybank.kafka.topic;

public enum TopicName {
    ACCOUNT_CREATED_TOPIC("account-created-topic"),
    ACCOUNT_WITHDRAWN_TOPIC("account-withdrawn-topic"),
    ACCOUNT_CREDITED_TOPIC("account-credited-topic");

    private final String topicName;

    TopicName(final String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
