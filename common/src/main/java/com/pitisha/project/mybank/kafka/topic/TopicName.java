package com.pitisha.project.mybank.kafka.topic;

public enum TopicName {
    ACCOUNT_CREATED_TOPIC("account-created-topic");

    private final String topicName;

    TopicName(final String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
