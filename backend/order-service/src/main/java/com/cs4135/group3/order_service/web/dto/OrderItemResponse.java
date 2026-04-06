package com.cs4135.group3.order_service.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
		Long id,
		UUID productId,
		String productName,
		BigDecimal price,
		Integer quantity
) {
}
