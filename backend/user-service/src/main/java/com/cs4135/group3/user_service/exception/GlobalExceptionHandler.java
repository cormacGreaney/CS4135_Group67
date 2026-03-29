package com.cs4135.group3.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> fieldErrors = new HashMap<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid");
		}
		ApiError body = ApiError.withFields(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				"Validation failed",
				request.getRequestURI(),
				fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleDuplicateEmail(EmailAlreadyExistsException ex, HttpServletRequest request) {
		ApiError body = ApiError.of(
				HttpStatus.CONFLICT.value(),
				"Conflict",
				ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
		ApiError body = ApiError.of(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				"Invalid email or password",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
		int code = status != null ? status.value() : ex.getStatusCode().value();
		String reason = status != null ? status.getReasonPhrase() : "Error";
		String message = ex.getReason() != null ? ex.getReason() : reason;
		ApiError body = ApiError.of(code, reason, message, request.getRequestURI());
		return ResponseEntity.status(ex.getStatusCode()).body(body);
	}

}
