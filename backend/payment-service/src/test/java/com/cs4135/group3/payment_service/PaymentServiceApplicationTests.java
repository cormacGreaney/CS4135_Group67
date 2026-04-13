package com.cs4135.group3.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"JWT_SECRET=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
		"app.payments.seed.enabled=false"
})
class PaymentServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void unauthenticatedRequestsReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/api/payments/00000000-0000-0000-0000-000000000001")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void authenticatedUserCanCreatePayment() throws Exception {
		String token = JwtTestTokens.accessToken("7", "CUSTOMER");

		MvcResult result = mockMvc.perform(post("/api/payments")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "orderId": 101,
								  "amount": 49.99,
								  "provider": "DemoProvider",
								  "forceFailure": false
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").value(101))
				.andExpect(jsonPath("$.userId").value(7))
				.andExpect(jsonPath("$.amount").value(49.99))
				.andExpect(jsonPath("$.provider").value("DemoProvider"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		String id = json.get("id").asText();

		mockMvc.perform(get("/api/payments/" + id)
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.userId").value(7));
	}

	@Test
	void userCannotReadAnotherUsersPayment() throws Exception {
		String ownerToken = JwtTestTokens.accessToken("7", "CUSTOMER");
		String otherUserToken = JwtTestTokens.accessToken("9", "CUSTOMER");

		MvcResult result = mockMvc.perform(post("/api/payments")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "orderId": 202,
								  "amount": 20.00,
								  "provider": "DemoProvider",
								  "forceFailure": false
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

		mockMvc.perform(get("/api/payments/" + id)
						.header("Authorization", "Bearer " + otherUserToken)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("You can only view your own payments"));
	}

	@Test
	void adminCanReadAnotherUsersPayment() throws Exception {
		String ownerToken = JwtTestTokens.accessToken("7", "CUSTOMER");
		String adminToken = JwtTestTokens.accessToken("2", "ADMINISTRATOR");

		MvcResult result = mockMvc.perform(post("/api/payments")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "orderId": 303,
								  "amount": 30.00,
								  "provider": "AdminDemo",
								  "forceFailure": false
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

		mockMvc.perform(get("/api/payments/" + id)
						.header("Authorization", "Bearer " + adminToken)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.userId").value(7));
	}

}
