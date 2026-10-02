package com.graduation.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(@NotBlank @Size(max = 30) String userCode,
		@NotBlank @Size(min = 6, max = 100) String password, @NotBlank @Size(max = 100) String fullName,
		@NotBlank @Email @Size(max = 100) String email, @Size(max = 20) String phone, @Size(max = 50) String className,
		@Size(max = 100) String department) {
}
