package com.graduation.notification.event;

public class RegistrationStatusChangedEvent {

    private Long registrationId;
    private Long userId;
    private Long topicId;
    private String status;

    public RegistrationStatusChangedEvent() {
    }

    public RegistrationStatusChangedEvent(
            Long registrationId,
            Long userId,
            Long topicId,
            String status) {

        this.registrationId = registrationId;
        this.userId = userId;
        this.topicId = topicId;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}