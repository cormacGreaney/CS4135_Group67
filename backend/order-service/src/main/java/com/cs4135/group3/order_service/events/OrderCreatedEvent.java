package com.cs4135.group3.order_service.events;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount
)
{
}