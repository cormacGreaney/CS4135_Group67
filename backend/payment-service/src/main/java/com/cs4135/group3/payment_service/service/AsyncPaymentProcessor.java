package com.cs4135.group3.payment_service.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs4135.group3.payment_service.domain.Payment;
import com.cs4135.group3.payment_service.domain.PaymentStatus;
import com.cs4135.group3.payment_service.integration.OrderPaymentCallbackClient;
import com.cs4135.group3.payment_service.messaging.OrderCreatedMessage;
import com.cs4135.group3.payment_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.payment_service.messaging.PaymentCompletedPublisher;
import com.cs4135.group3.payment_service.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncPaymentProcessor {

	private final PaymentRepository paymentRepository;
	private final PaymentCompletedPublisher paymentCompletedPublisher;
	private final OrderPaymentCallbackClient orderPaymentCallbackClient;

	@Transactional
	public void processOrderCreated(OrderCreatedMessage message) {
		if (paymentRepository.existsByOrderId(message.orderId())) {
			log.info("Payment already recorded for order {}, skipping duplicate message", message.orderId());
			return;
		}

		// Simulated payment: always succeeds (set false or use business rules to test failures).
		boolean success = true;

		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setOrderId(message.orderId());
		payment.setUserId(message.userId());
		payment.setAmount(message.totalAmount());
		payment.setProvider("async-rabbitmq");
		payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
		payment.setPaymentDate(Instant.now());
		paymentRepository.save(payment);

		PaymentCompletedMessage completed = new PaymentCompletedMessage(
				payment.getId(),
				message.orderId(),
				message.userId(),
				message.totalAmount(),
				payment.getStatus().name());

		paymentCompletedPublisher.publish(completed);
		orderPaymentCallbackClient.notifyOrderService(completed);
	}
}
