package com.cs4135.group3.order_service.web.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productName,
		BigDecimal price,
		Integer quantity
) {
}
