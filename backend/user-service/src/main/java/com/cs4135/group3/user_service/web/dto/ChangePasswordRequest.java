package com.cs4135.group3.user_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
		@NotBlank(message = "Current password is required")
		@Size(min = 8, max = 128, message = "Current password must be between 8 and 128 characters")
		String currentPassword,

		@NotBlank(message = "New password is required")
		@Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
		String newPassword
) {
}
