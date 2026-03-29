package com.cs4135.group3.user_service.web.dto;

import com.cs4135.group3.user_service.domain.UserRole;

import java.time.Instant;

public record UserResponse(
		Long userId,
		String email,
		UserRole role,
		Instant createdAt
) {
}
