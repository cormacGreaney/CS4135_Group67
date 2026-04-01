package com.cs4135.group3.payment_service.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
		@NotNull(message = "Order ID is required")
		@Positive(message = "Order ID must be positive")
		Long orderId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
		@Digits(integer = 10, fraction = 2, message = "Amount must have up to 2 decimal places")
		BigDecimal amount,

		@NotBlank(message = "Provider is required")
		@Size(max = 100, message = "Provider must be at most 100 characters")
		String provider,

		boolean forceFailure
) {
}
