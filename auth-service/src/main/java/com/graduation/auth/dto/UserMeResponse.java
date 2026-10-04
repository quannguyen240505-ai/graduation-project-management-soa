package com.graduation.auth.dto;

import com.graduation.auth.entity.Role;
import com.graduation.auth.entity.UserStatus;

import java.time.LocalDateTime;

public record UserMeResponse(Long id, String userCode, String fullName, String email, String phone, Role role,
		String className, String department, UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}