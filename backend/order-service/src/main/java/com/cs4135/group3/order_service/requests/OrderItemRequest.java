package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;

public record OrderItemRequest(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity
)
{

}