package com.cs4135.group3.user_service;

import com.cs4135.group3.user_service.config.JwtProperties;
import com.cs4135.group3.user_service.support.AbstractUserServiceIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
class UserServiceApplicationTests extends AbstractUserServiceIntegrationTest{

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	JwtProperties jwtProperties;

	@Test
	void contextLoads() {
	}

	@Test
	void unauthenticatedMeReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registerLoginAndFetchProfile() throws Exception {
		String email = "user-" + UUID.randomUUID() + "@example.com";
		String body = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresInSeconds").isNumber());

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andReturn();

		JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
		String token = loginJson.get("accessToken").asText();

		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email.toLowerCase()))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	void duplicateRegisterReturnsConflict() throws Exception {
		String email = "dup-" + UUID.randomUUID() + "@example.com";
		String body = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Conflict"));
	}

	@Test
	void invalidLoginReturnsUnauthorized() throws Exception {
		String body = "{ \"email\": \"nope@example.com\", \"password\": \"wrongwrong\" }";
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void actuatorHealthIsUpWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/actuator/health").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void meWithMalformedBearerTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer not-a-valid-jwt")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meWithWrongSignatureTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + JwtTestTokens.signedWithWrongSecret())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meWithExpiredTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + JwtTestTokens.expired(jwtProperties))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meWithMissingRoleClaimReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + JwtTestTokens.missingRoleClaim(jwtProperties))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meWithEmptyBearerTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer ")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void changePasswordRequiresAuthentication() throws Exception {
		mockMvc.perform(put("/api/users/me/password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"currentPassword\": \"password12\", \"newPassword\": \"newpassword12\" }"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void changePasswordWithWrongCurrentPasswordReturnsUnauthorized() throws Exception {
		String email = "cpw-" + UUID.randomUUID() + "@example.com";
		String reg = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isOk())
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"currentPassword\": \"wrongpass1\", \"newPassword\": \"newpassword12\" }"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Current password is incorrect"));
	}

	@Test
	void changePasswordRejectsNewPasswordTooShort() throws Exception {
		String email = "cpshort-" + UUID.randomUUID() + "@example.com";
		String reg = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isOk())
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"currentPassword\": \"password12\", \"newPassword\": \"short\" }"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changePasswordRejectsWhenNewEqualsCurrent() throws Exception {
		String email = "cpsame-" + UUID.randomUUID() + "@example.com";
		String reg = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isOk())
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"currentPassword\": \"password12\", \"newPassword\": \"password12\" }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("New password must be different from the current password"));
	}

	@Test
	void changePasswordThenLoginWithNewPassword() throws Exception {
		String email = "cpok-" + UUID.randomUUID() + "@example.com";
		String reg = "{ \"email\": \"" + email + "\", \"password\": \"password12\" }";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isOk())
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"currentPassword\": \"password12\", \"newPassword\": \"newpassword12\" }"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(reg))
				.andExpect(status().isUnauthorized());
		String newLogin = "{ \"email\": \"" + email + "\", \"password\": \"newpassword12\" }";
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(newLogin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString());
	}

}
