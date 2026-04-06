package com.cs4135.group3.order_service.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemQuantityUpdateRequest(
		@NotNull @Min(1) Integer amount
) {
}
