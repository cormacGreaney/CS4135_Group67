package com.cs4135.group3.payment_service.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CardCheckoutRequest(
		@NotNull(message = "Order ID is required")
		@Positive(message = "Order ID must be positive")
		Long orderId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
		@Digits(integer = 10, fraction = 2, message = "Amount must have up to 2 decimal places")
		BigDecimal amount,

		@NotBlank(message = "Card number is required")
		@Pattern(regexp = "^[0-9]{12,19}$", message = "Card number must be 12 to 19 digits")
		String cardNumber,

		@NotBlank(message = "Card holder name is required")
		@Size(max = 100, message = "Card holder name must be at most 100 characters")
		String cardHolderName,

		@NotNull(message = "Expiry month is required")
		@Min(value = 1, message = "Expiry month must be between 1 and 12")
		@Max(value = 12, message = "Expiry month must be between 1 and 12")
		Integer expiryMonth,

		@NotNull(message = "Expiry year is required")
		@Min(value = 2020, message = "Expiry year is invalid")
		Integer expiryYear,

		@NotBlank(message = "CVV is required")
		@Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
		String cvv
) {
}
