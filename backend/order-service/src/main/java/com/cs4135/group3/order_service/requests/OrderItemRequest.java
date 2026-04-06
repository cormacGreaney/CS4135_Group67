package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        UUID productId,
        String productName,
        BigDecimal price,
        Integer quantity
)
{

}
