package com.cs4135.group3.order_service.messaging;

public final class MessagingConstants {

	private MessagingConstants() {
	}

	public static final String ECOMMERCE_TOPIC_EXCHANGE = "ecommerce.topic";

	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

	public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

	public static final String ORDER_PAYMENT_COMPLETED_QUEUE = "q.order.payment.completed";
}
