package com.cs4135.group3.product_service;

import com.cs4135.group3.product_service.config.InternalApiTokenFilter;
import com.cs4135.group3.product_service.config.SecurityConfig;
import com.cs4135.group3.product_service.security.JwtAuthenticationFilter;
import com.cs4135.group3.product_service.service.ProductService;
import com.cs4135.group3.product_service.service.TokenService;
import com.cs4135.group3.product_service.web.InternalProductController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalProductController.class)
@Import({SecurityConfig.class, InternalApiTokenFilter.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = "app.internal-api.token=test-internal-token")
class InternalProductControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProductService productService;

	@MockBean
	private TokenService tokenService;

	@Test
	void deductStockRejectsMissingInternalToken() throws Exception {
		// Internal endpoints must not be reachable from anonymous callers.
		mockMvc.perform(post("/internal/products/stock/deduct")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"items", new Object[] {
										Map.of("productId", "b0000000-0000-4000-8000-000000000011", "quantity", 1)
								}))))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(productService);
	}

	@Test
	void deductStockAllowsValidInternalToken() throws Exception {
		// A trusted backend service can call the endpoint when it sends the shared token header.
		mockMvc.perform(post("/internal/products/stock/deduct")
						.header(InternalApiTokenFilter.HEADER, "test-internal-token")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"items", new Object[] {
										Map.of("productId", "b0000000-0000-4000-8000-000000000011", "quantity", 2)
								}))))
				.andExpect(status().isNoContent());

		verify(productService).deductStock(anyList());
	}
}
