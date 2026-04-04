package com.cs4135.group3.payment_service.messaging;

import java.math.BigDecimal;
import java.util.UUID;


public record PaymentCompletedMessage(
		UUID paymentId,
		Long orderId,
		Long userId,
		BigDecimal amount,
		String status
) {
}
