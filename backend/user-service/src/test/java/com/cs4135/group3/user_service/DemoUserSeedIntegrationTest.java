package com.cs4135.group3.user_service;

import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.domain.UserRole;
import com.cs4135.group3.user_service.repository.UserRepository;
import com.cs4135.group3.user_service.support.AbstractUserServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@TestPropertySource(
		properties = {
			"app.users.seed.enabled=true",
			"app.users.seed.demo-password=password12"
		})
class DemoUserSeedIntegrationTest extends AbstractUserServiceIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	UserRepository userRepository;

	@Autowired
	MockMvc mockMvc;

	@Test
	void seedsDemoCustomersAndTheyCanLogin() throws Exception {
		User u1 = userRepository.findByEmailIgnoreCase("customer.demo@example.com").orElseThrow();
		User u2 = userRepository.findByEmailIgnoreCase("customer2.demo@example.com").orElseThrow();
		assertThat(u1.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(u2.getRole()).isEqualTo(UserRole.CUSTOMER);

		String body = "{ \"email\": \"customer.demo@example.com\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString());
	}
}
