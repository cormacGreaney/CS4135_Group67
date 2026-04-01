package com.cs4135.group3.payment_service.web.dto;

import com.cs4135.group3.payment_service.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
		UUID id,
		Long orderId,
		Long userId,
		BigDecimal amount,
		String provider,
		PaymentStatus status,
		Instant paymentDate
) {
}
