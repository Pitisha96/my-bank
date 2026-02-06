package com.pitisha.project.mybank.kafka.topic;

public enum TopicName {
    ACCOUNT_OPERATIONS_TOPIC("account-operations-topic"),
    ACCOUNT_CREATED_TOPIC("account-created-topic");

    private final String topicName;

    TopicName(final String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
