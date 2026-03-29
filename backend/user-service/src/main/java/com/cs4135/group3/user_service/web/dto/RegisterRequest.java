package com.cs4135.group3.user_service.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Must be a valid email")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
		String password
) {
}
