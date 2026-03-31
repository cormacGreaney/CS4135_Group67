package com.cs4135.group3.product_service.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
		UUID id,
		String name,
		String description,
		BigDecimal price,
		int stockQuantity,
		String category,
		Instant createdAt
) {
}
