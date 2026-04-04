package com.cs4135.group3.order_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.cs4135.group3.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedListener {

	private final OrderService orderService;

	@RabbitListener(queues = MessagingConstants.ORDER_PAYMENT_COMPLETED_QUEUE)
	public void onPaymentCompleted(PaymentCompletedMessage message) {
		log.debug("PaymentCompleted from RabbitMQ: orderId={}", message.orderId());
		orderService.applyPaymentResult(message);
	}
}
