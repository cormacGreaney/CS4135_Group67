package com.cs4135.group3.payment_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.cs4135.group3.payment_service.repository.PaymentRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(
		properties = {
			"JWT_SECRET=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
			"app.payments.seed.enabled=true"
		})
class SeededDemoPaymentIntegrationTest {

	private static final UUID SEED_PAYMENT_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	private static final long SEED_ORDER_ID = 5001L;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	PaymentRepository paymentRepository;

}
