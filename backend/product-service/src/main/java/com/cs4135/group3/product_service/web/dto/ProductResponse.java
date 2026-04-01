package com.cs4135.group3.product_service.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// What the API returns for one product (safe fields only — nothing internal)
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
