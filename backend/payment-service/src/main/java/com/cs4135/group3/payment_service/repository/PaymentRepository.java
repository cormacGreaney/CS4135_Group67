package com.cs4135.group3.payment_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs4135.group3.payment_service.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	List<Payment> findByOrderIdOrderByPaymentDateDesc(Long orderId);

	List<Payment> findByOrderIdAndUserIdOrderByPaymentDateDesc(Long orderId, Long userId);
}
