package com.cs4135.group3.order_service;

import com.cs4135.group3.order_service.config.InternalApiTokenFilter;
import com.cs4135.group3.order_service.config.SecurityConfig;
import com.cs4135.group3.order_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.order_service.security.JwtAuthenticationFilter;
import com.cs4135.group3.order_service.service.OrderService;
import com.cs4135.group3.order_service.service.TokenService;
import com.cs4135.group3.order_service.web.InternalOrderPaymentController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalOrderPaymentController.class)
@Import({SecurityConfig.class, InternalApiTokenFilter.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = "app.internal-api.token=test-internal-token")
class InternalOrderPaymentControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@MockBean
	private TokenService tokenService;

	@Test
	void paymentResultRejectsMissingInternalToken() throws Exception {
		// Payment callbacks are internal-only and must be rejected without the shared token.
		PaymentCompletedMessage message = new PaymentCompletedMessage(
				UUID.randomUUID(),
				10L,
				42L,
				new BigDecimal("19.99"),
				"SUCCESS");

		mockMvc.perform(post("/internal/orders/10/payment-result")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(message)))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(orderService);
	}

	@Test
	void paymentResultAllowsValidInternalToken() throws Exception {
		// The payment service can deliver the callback when it presents the shared token.
		PaymentCompletedMessage message = new PaymentCompletedMessage(
				UUID.randomUUID(),
				10L,
				42L,
				new BigDecimal("19.99"),
				"SUCCESS");

		mockMvc.perform(post("/internal/orders/10/payment-result")
						.header(InternalApiTokenFilter.HEADER, "test-internal-token")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(message)))
				.andExpect(status().isNoContent());

		verify(orderService).applyPaymentResult(message);
	}
}
