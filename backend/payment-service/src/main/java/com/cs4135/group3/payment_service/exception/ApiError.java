package com.cs4135.group3.payment_service.exception;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors
) {

	public static ApiError of(int status, String error, String message, String path) {
		return new ApiError(Instant.now(), status, error, message, path, null);
	}

	public static ApiError withFields(int status, String error, String message, String path, Map<String, String> fieldErrors) {
		return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
	}
}
