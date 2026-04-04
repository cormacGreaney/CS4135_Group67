package com.cs4135.group3.payment_service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(PaymentCompletedMessage message) {
		try {
			rabbitTemplate.convertAndSend(
					MessagingConstants.ECOMMERCE_TOPIC_EXCHANGE,
					MessagingConstants.PAYMENT_COMPLETED_ROUTING_KEY,
					message);
			log.debug("Published PaymentCompleted for orderId={}", message.orderId());
		}
		catch (Exception ex) {
			log.error("Failed to publish PaymentCompleted for orderId={}", message.orderId(), ex);
		}
	}
}
