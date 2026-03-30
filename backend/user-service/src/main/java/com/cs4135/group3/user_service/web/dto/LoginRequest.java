package com.cs4135.group3.user_service.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Must be a valid email")
		String email,

		@NotBlank(message = "Password is required")
		String password
) {
}
