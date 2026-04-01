package com.cs4135.group3.payment_service.repository;

import com.cs4135.group3.payment_service.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	List<Payment> findByOrderIdOrderByPaymentDateDesc(Long orderId);
}
