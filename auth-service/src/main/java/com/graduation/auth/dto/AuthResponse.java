package com.graduation.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, Long userId, String userCode,
		String fullName, String role) {
}
