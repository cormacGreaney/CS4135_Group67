package com.cs4135.group3.user_service;

import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.domain.UserRole;
import com.cs4135.group3.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class BootstrapAdminIntegrationTest {

	private static final String BOOTSTRAP_EMAIL = "bootstrap-" + UUID.randomUUID() + "@example.com";

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void bootstrapProps(DynamicPropertyRegistry registry) {
		registry.add("app.bootstrap.admin.email", () -> BOOTSTRAP_EMAIL);
		registry.add("app.bootstrap.admin.password", () -> "password12");
	}

	@Autowired
	UserRepository userRepository;

	@Autowired
	MockMvc mockMvc;

	@Test
	void createsAdministratorWhenConfigured() {
		User user = userRepository.findByEmailIgnoreCase(BOOTSTRAP_EMAIL).orElseThrow();
		assertThat(user.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
	}

	@Test
	void bootstrapAdminCanLoginWithAdministratorRole() throws Exception {
		String body = "{ \"email\": \"" + BOOTSTRAP_EMAIL + "\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString());
	}
}
