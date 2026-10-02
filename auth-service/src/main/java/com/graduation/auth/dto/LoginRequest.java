package com.graduation.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "UserCode không được để trống") String userCode,
		@NotBlank(message = "Password không được để trống") String password) {
}
