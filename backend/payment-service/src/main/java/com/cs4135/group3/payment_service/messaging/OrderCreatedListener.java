package com.cs4135.group3.payment_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cs4135.group3.payment_service.service.AsyncPaymentProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

	private final AsyncPaymentProcessor asyncPaymentProcessor;
	@Value("${app.payment.auto-from-order-created:false}")
	private boolean autoPayFromOrderCreated;

	@RabbitListener(queues = MessagingConstants.PAYMENT_ORDER_CREATED_QUEUE)
	public void onOrderCreated(OrderCreatedMessage message) {
		log.debug("OrderCreated received: orderId={}", message.orderId());
		if (!autoPayFromOrderCreated) {
			log.info("Skipping auto payment for orderId={} (app.payment.auto-from-order-created=false)",
					message.orderId());
			return;
		}
		asyncPaymentProcessor.processOrderCreated(message);
	}
}
