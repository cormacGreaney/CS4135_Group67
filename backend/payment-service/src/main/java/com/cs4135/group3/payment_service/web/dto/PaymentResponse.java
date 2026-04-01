package com.cs4135.group3.payment_service.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.cs4135.group3.payment_service.domain.PaymentStatus;

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
