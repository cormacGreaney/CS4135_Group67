package com.cs4135.group3.product_service.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Body for POST /api/products — what an admin sends to create a product
public record ProductCreateRequest(
		@NotBlank @Size(max = 255) String name,
		@Size(max = 10_000) String description,
		@NotNull @DecimalMin(value = "0.0", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal price,
		@Min(0) int stockQuantity,
		@NotBlank @Size(max = 255) String category
) {
}
