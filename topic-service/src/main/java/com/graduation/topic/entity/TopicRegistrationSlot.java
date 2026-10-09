package com.graduation.topic.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Lịch sử chỗ của Topic; registrationId chỉ là ID nhận qua REST. */
@Entity
@Table(name = "topic_registration_slots",
        indexes = @Index(name = "idx_topic_slots_topic_id", columnList = "topic_id"))
public class TopicRegistrationSlot {

    @Id
    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "capacity_version", nullable = false)
    private long capacityVersion;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public TopicRegistrationSlot() {
    }

    public TopicRegistrationSlot(Long registrationId, Long topicId) {
        this.registrationId = registrationId;
        this.topicId = topicId;
    }

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public Long getRegistrationId() { return registrationId; }
    public Long getTopicId() { return topicId; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public long getCapacityVersion() { return capacityVersion; }
    public void setCapacityVersion(long version) { this.capacityVersion = version; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
