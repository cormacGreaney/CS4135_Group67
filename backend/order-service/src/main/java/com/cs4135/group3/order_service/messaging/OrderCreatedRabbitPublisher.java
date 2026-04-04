package com.cs4135.group3.order_service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.cs4135.group3.order_service.events.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedRabbitPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(OrderCreatedEvent event) {
		try {
			rabbitTemplate.convertAndSend(
					MessagingConstants.ECOMMERCE_TOPIC_EXCHANGE,
					MessagingConstants.ORDER_CREATED_ROUTING_KEY,
					event);
			log.debug("Published OrderCreated to RabbitMQ: orderId={}", event.orderId());
		}
		catch (Exception ex) {
			// Order is already persisted; do not fail the HTTP response if the broker is down.
			log.error("Failed to publish OrderCreated for orderId={}", event.orderId(), ex);
		}
	}
}
