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

	@Test
	void seedRunnerInsertsExpectedRow() {
		assertThat(paymentRepository.findById(SEED_PAYMENT_ID)).isPresent();
		assertThat(paymentRepository.findById(SEED_PAYMENT_ID).orElseThrow().getUserId()).isEqualTo(1L);
		assertThat(paymentRepository.findById(SEED_PAYMENT_ID).orElseThrow().getOrderId()).isEqualTo(SEED_ORDER_ID);
	}

	@Test
	void ownerCanFetchSeededPaymentById() throws Exception {
		String token = JwtTestTokens.accessToken("1", "CUSTOMER");
		mockMvc.perform(get("/api/payments/" + SEED_PAYMENT_ID)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(SEED_PAYMENT_ID.toString()))
				.andExpect(jsonPath("$.orderId").value((int) SEED_ORDER_ID))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.amount").value(49.99))
				.andExpect(jsonPath("$.provider").value("DemoProvider"))
				.andExpect(jsonPath("$.status").value("SUCCESS"));
	}

	@Test
	void otherCustomerCannotFetchSeededPaymentById() throws Exception {
		String token = JwtTestTokens.accessToken("9", "CUSTOMER");
		mockMvc.perform(get("/api/payments/" + SEED_PAYMENT_ID)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("You can only view your own payments"));
	}

	@Test
	void administratorCanFetchSeededPaymentById() throws Exception {
		String token = JwtTestTokens.accessToken("2", "ADMINISTRATOR");
		mockMvc.perform(get("/api/payments/" + SEED_PAYMENT_ID)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1));
	}

	@Test
	void ownerListsSeededPaymentByOrderId() throws Exception {
		String token = JwtTestTokens.accessToken("1", "CUSTOMER");
		mockMvc.perform(get("/api/payments/order/" + SEED_ORDER_ID)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].id").value(SEED_PAYMENT_ID.toString()))
				.andExpect(jsonPath("$[0].orderId").value((int) SEED_ORDER_ID));
	}

	@Test
	void otherCustomerSeesNoPaymentsForSeededOrderId() throws Exception {
		String token = JwtTestTokens.accessToken("9", "CUSTOMER");
		mockMvc.perform(get("/api/payments/order/" + SEED_ORDER_ID)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}
}
