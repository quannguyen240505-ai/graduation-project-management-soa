package com.graduation.topic.dto;

import java.time.LocalDateTime;

import com.graduation.topic.entity.Topic;

/**
 * Thông tin đề tài trả về cho client.
 */
public class TopicResponse {

    private Long id;
    private String topicCode;
    private String topicName;
    private String description;
    private Long lecturerId;
    private Integer maxStudents;
    private Integer currentStudents;
    private String field;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển dữ liệu từ Entity sang dữ liệu phản hồi.
     */
    public static TopicResponse fromEntity(Topic topic) {
        TopicResponse response = new TopicResponse();

        response.id = topic.getId();
        response.topicCode = topic.getTopicCode();
        response.topicName = topic.getTopicName();
        response.description = topic.getDescription();
        response.lecturerId = topic.getLecturerId();
        response.maxStudents = topic.getMaxStudents();
        response.currentStudents = topic.getCurrentStudents();
        response.field = topic.getField();
        response.status = topic.getStatus();
        response.createdAt = topic.getCreatedAt();
        response.updatedAt = topic.getUpdatedAt();

        return response;
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