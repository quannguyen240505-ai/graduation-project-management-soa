package com.group.registration.dto;

import lombok.Data;

@Data
public class TopicDTO {
    private Long id;
    private String topicName;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status; // Cần kiểm tra status = "OPEN"
}