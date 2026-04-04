package com.cs4135.group3.payment_service.messaging;

import java.math.BigDecimal;

public record OrderCreatedMessage(Long orderId, Long userId, BigDecimal totalAmount) {
}
