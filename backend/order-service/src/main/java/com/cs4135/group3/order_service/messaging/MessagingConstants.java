package com.cs4135.group3.order_service.messaging;

/**
 * Names shared with other services (e.g. payment-service) that bind queues to the same exchange and routing keys.
 */
public final class MessagingConstants {

	private MessagingConstants() {
	}

	/** Durable topic exchange for e-commerce domain events. */
	public static final String ECOMMERCE_TOPIC_EXCHANGE = "ecommerce.topic";

	/** Routing key when a new order is persisted in PENDING state. */
	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
}
