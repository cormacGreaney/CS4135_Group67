package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddOrderItemRequest(
		@NotNull UUID productId,
		@NotBlank String productName,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
		@NotNull @Min(1) Integer quantity
) {
}
