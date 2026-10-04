package com.graduation.topic.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Lưu thông tin đề tài, tương ứng với bảng detai.
 */
@Entity
@Table(name = "detai")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_code", nullable = false,
            unique = true, length = 50)
    private String topicCode;

    @Column(name = "topic_name", nullable = false, length = 255)
    private String topicName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "current_students", nullable = false)
    private Integer currentStudents = 0;

    @Column(name = "field", length = 100)
    private String field;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false,
            updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Topic() {
    }

    /**
     * Ghi thời gian khi tạo đề tài.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Cập nhật thời gian khi sửa đề tài.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTopicCode() {
		return topicCode;
	}

	public void setTopicCode(String topicCode) {
		this.topicCode = topicCode;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getLecturerId() {
		return lecturerId;
	}

	public void setLecturerId(Long lecturerId) {
		this.lecturerId = lecturerId;
	}

	public Integer getMaxStudents() {
		return maxStudents;
	}

	public void setMaxStudents(Integer maxStudents) {
		this.maxStudents = maxStudents;
	}

	public Integer getCurrentStudents() {
		return currentStudents;
	}

	public void setCurrentStudents(Integer currentStudents) {
		this.currentStudents = currentStudents;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}