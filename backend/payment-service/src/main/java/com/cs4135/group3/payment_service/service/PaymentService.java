package com.cs4135.group3.payment_service.service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cs4135.group3.payment_service.domain.Payment;
import com.cs4135.group3.payment_service.domain.PaymentStatus;
import com.cs4135.group3.payment_service.integration.OrderPaymentCallbackClient;
import com.cs4135.group3.payment_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.payment_service.messaging.PaymentCompletedPublisher;
import com.cs4135.group3.payment_service.repository.PaymentRepository;
import com.cs4135.group3.payment_service.web.dto.CardCheckoutRequest;
import com.cs4135.group3.payment_service.web.dto.CreatePaymentRequest;
import com.cs4135.group3.payment_service.web.dto.PaymentResponse;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentCompletedPublisher paymentCompletedPublisher;
	private final OrderPaymentCallbackClient orderPaymentCallbackClient;

	public PaymentService(
			PaymentRepository paymentRepository,
			PaymentCompletedPublisher paymentCompletedPublisher,
			OrderPaymentCallbackClient orderPaymentCallbackClient) {
		this.paymentRepository = paymentRepository;
		this.paymentCompletedPublisher = paymentCompletedPublisher;
		this.orderPaymentCallbackClient = orderPaymentCallbackClient;
	}

	@Transactional
	public PaymentResponse create(CreatePaymentRequest req, Authentication authentication) {
		Long userId = parseUserId(authentication);

		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setOrderId(req.orderId());
		payment.setUserId(userId);
		payment.setAmount(req.amount());
		payment.setProvider(req.provider().trim());
		payment.setStatus(req.forceFailure() ? PaymentStatus.FAILED : PaymentStatus.SUCCESS);
		payment.setPaymentDate(Instant.now());

		paymentRepository.save(payment);
		return toResponse(payment);
	}

	@Transactional
	public PaymentResponse checkoutCard(CardCheckoutRequest req, Authentication authentication) {
		Long userId = parseUserId(authentication);

		if (paymentRepository.existsByOrderId(req.orderId())) {
			throw new ResponseStatusException(BAD_REQUEST, "Payment already exists for this order");
		}

		if (!passesLuhn(req.cardNumber())) {
			throw new ResponseStatusException(BAD_REQUEST, "Card number failed validation");
		}

		boolean approved = !isExpired(req.expiryMonth(), req.expiryYear())
				&& !req.cardNumber().endsWith("0000");

		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setOrderId(req.orderId());
		payment.setUserId(userId);
		payment.setAmount(req.amount());
		payment.setProvider("sim-card");
		payment.setStatus(approved ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
		payment.setPaymentDate(Instant.now());

		paymentRepository.save(payment);

		PaymentCompletedMessage completed = new PaymentCompletedMessage(
				payment.getId(),
				payment.getOrderId(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getStatus().name());
		paymentCompletedPublisher.publish(completed);
		orderPaymentCallbackClient.notifyOrderService(completed);

		return toResponse(payment);
	}

	private static boolean isExpired(Integer expiryMonth, Integer expiryYear) {
		YearMonth now = YearMonth.now();
		YearMonth cardExpiry = YearMonth.of(expiryYear, expiryMonth);
		return cardExpiry.isBefore(now);
	}

	private static boolean passesLuhn(String cardNumber) {
		int sum = 0;
		boolean shouldDouble = false;
		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			int digit = cardNumber.charAt(i) - '0';
			if (shouldDouble) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}
			sum += digit;
			shouldDouble = !shouldDouble;
		}
		return sum % 10 == 0;
	}

	public PaymentResponse getById(UUID id, Authentication authentication) {
		Payment payment = paymentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));
		enforceOwnership(payment, authentication);
		return toResponse(payment);
	}

	public List<PaymentResponse> getByOrderId(Long orderId, Authentication authentication) {
		if (isAdmin(authentication)) {
			return paymentRepository.findByOrderIdOrderByPaymentDateDesc(orderId)
					.stream()
					.map(PaymentService::toResponse)
					.toList();
		}

		Long userId = parseUserId(authentication);
		return paymentRepository.findByOrderIdAndUserIdOrderByPaymentDateDesc(orderId, userId)
				.stream()
				.map(PaymentService::toResponse)
				.toList();
	}

	private void enforceOwnership(Payment payment, Authentication authentication) {
		if (isAdmin(authentication)) {
			return;
		}

		Long userId = parseUserId(authentication);
		if (!payment.getUserId().equals(userId)) {
			throw new ResponseStatusException(FORBIDDEN, "You can only view your own payments");
		}
	}

	private Long parseUserId(Authentication authentication) {
		try {
			return Long.valueOf(authentication.getName());
		}
		catch (NumberFormatException ex) {
			throw new ResponseStatusException(FORBIDDEN, "Invalid authenticated user");
		}
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ROLE_ADMINISTRATOR"::equals);
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
