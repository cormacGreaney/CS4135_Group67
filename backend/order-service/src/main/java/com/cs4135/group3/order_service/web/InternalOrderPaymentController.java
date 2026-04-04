package com.cs4135.group3.order_service.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cs4135.group3.order_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderPaymentController {

	private final OrderService orderService;

	@PostMapping("/{orderId}/payment-result")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void paymentResult(
			@PathVariable Long orderId,
			@RequestBody PaymentCompletedMessage body) {
		if (!orderId.equals(body.orderId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path orderId does not match body orderId");
		}
		orderService.applyPaymentResult(body);
	}
}
