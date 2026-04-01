package com.cs4135.group3.payment_service.service;

import com.cs4135.group3.payment_service.domain.Payment;
import com.cs4135.group3.payment_service.domain.PaymentStatus;
import com.cs4135.group3.payment_service.repository.PaymentRepository;
import com.cs4135.group3.payment_service.web.dto.CreatePaymentRequest;
import com.cs4135.group3.payment_service.web.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Transactional
	public PaymentResponse create(CreatePaymentRequest req) {
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setOrderId(req.orderId());
		payment.setUserId(req.userId());
		payment.setAmount(req.amount());
		payment.setProvider(req.provider().trim());
		payment.setStatus(req.forceFailure() ? PaymentStatus.FAILED : PaymentStatus.SUCCESS);
		payment.setPaymentDate(Instant.now());

		paymentRepository.save(payment);
		return toResponse(payment);
	}

	public PaymentResponse getById(UUID id) {
		return paymentRepository.findById(id)
				.map(PaymentService::toResponse)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));
	}

	public List<PaymentResponse> getByOrderId(Long orderId) {
		return paymentRepository.findByOrderIdOrderByPaymentDateDesc(orderId)
				.stream()
				.map(PaymentService::toResponse)
				.toList();
	}

	private static PaymentResponse toResponse(Payment payment) {
		return new PaymentResponse(
				payment.getId(),
				payment.getOrderId(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getProvider(),
				payment.getStatus(),
				payment.getPaymentDate());
	}
}
