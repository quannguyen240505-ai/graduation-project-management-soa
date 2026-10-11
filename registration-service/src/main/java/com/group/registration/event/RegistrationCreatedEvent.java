package com.group.registration.event;

/**
 * Event published when a student successfully registers for a topic.
 */
public class RegistrationCreatedEvent {

    private Long registrationId;
    private Long userId;
    private Long topicId;
    private String topicName;

    public RegistrationCreatedEvent() {
    }

    public RegistrationCreatedEvent(
            Long registrationId,
            Long userId,
            Long topicId,
            String topicName) {

        this.registrationId = registrationId;
        this.userId = userId;
        this.topicId = topicId;
        this.topicName = topicName;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}