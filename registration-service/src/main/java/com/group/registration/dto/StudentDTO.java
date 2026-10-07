package com.group.registration.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private Long id;
    private String studentCode;
    private String fullName;
    private String role; // Cần kiểm tra role = "STUDENT"
    private String status;
}