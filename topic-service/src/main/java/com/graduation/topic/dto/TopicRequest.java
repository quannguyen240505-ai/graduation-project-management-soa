package com.graduation.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu đầu vào khi tạo hoặc cập nhật đề tài.
 */
public class TopicRequest {

    @NotBlank(message = "Mã đề tài không được để trống")
    @Size(max = 50, message = "Mã đề tài tối đa 50 ký tự")
    private String topicCode;

    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 255, message = "Tên đề tài tối đa 255 ký tự")
    private String topicName;

    private String description;

    @NotNull(message = "Phải chọn giảng viên")
    @Positive(message = "ID giảng viên phải lớn hơn 0")
    private Long lecturerId;

    @NotNull(message = "Phải nhập số lượng sinh viên tối đa")
    @Positive(message = "Số lượng sinh viên tối đa phải lớn hơn 0")
    private Integer maxStudents;

    @Size(max = 100, message = "Lĩnh vực tối đa 100 ký tự")
    private String field;

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

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}