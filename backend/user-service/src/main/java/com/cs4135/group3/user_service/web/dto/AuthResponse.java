package com.cs4135.group3.user_service.web.dto;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds
) {
}
