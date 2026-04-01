package com.cs4135.group3.payment_service.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cs4135.group3.payment_service.service.PaymentService;
import com.cs4135.group3.payment_service.web.dto.CreatePaymentRequest;
import com.cs4135.group3.payment_service.web.dto.PaymentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest body, Authentication authentication) {
		return paymentService.create(body, authentication);
	}

	@GetMapping("/{id}")
	public PaymentResponse getById(@PathVariable UUID id, Authentication authentication) {
		return paymentService.getById(id, authentication);
	}

	@GetMapping("/order/{orderId}")
	public List<PaymentResponse> getByOrderId(@PathVariable Long orderId, Authentication authentication) {
		return paymentService.getByOrderId(orderId, authentication);
	}
}
