package com.cs4135.group3.order_service.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;
import com.cs4135.group3.order_service.model.OrderStatus;

public record OrderResponse(
		Long id,
		Long userId,
		String orderNumber,
		BigDecimal totalPrice,
		OrderStatus status,
		LocalDateTime orderedDate,
		List<OrderItemResponse> items
) {
	public static OrderResponse fromEntity(Order order) {
		List<OrderItemResponse> lines = order.getOrderItems() == null
				? List.of()
				: order.getOrderItems().stream().map(OrderResponse::fromItem).toList();
		return new OrderResponse(
				order.getId(),
				order.getUserId(),
				order.getOrderNumber(),
				order.getTotalPrice(),
				order.getStatus(),
				order.getOrderedDate(),
				lines);
	}

	private static OrderItemResponse fromItem(OrderItem item) {
		return new OrderItemResponse(
				item.getId(),
				item.getProductId(),
				item.getProductName(),
				item.getPrice(),
				item.getQuantity());
	}
}
