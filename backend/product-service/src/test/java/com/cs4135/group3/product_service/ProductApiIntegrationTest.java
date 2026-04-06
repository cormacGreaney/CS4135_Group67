package com.cs4135.group3.product_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// End-to-end tests: real MySQL in Docker (Testcontainers), fake JWTs signed with the test secret.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductApiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("product_service")
            .withUsername("root")
            .withPassword("mysql")
            .withUrlParam("allowPublicKeyRetrieval", "true")
            .withUrlParam("serverTimezone", "UTC");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Order(1)
    void listIsPublicAndReturnsSeededProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(13)));
    }

    @Test
    @Order(2)
    void getByIdReturnsProduct() throws Exception {
        mockMvc.perform(get("/api/products/b0000000-0000-4000-8000-000000000011"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Guinness Draught 8 x 500ml"))
    }

    @Test
    @Order(3)
    void postWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "X",
                                "description", "d",
                                "price", new BigDecimal("1.00"),
                                "stockQuantity", 1,
                                "category", "Cat"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void postWithCustomerTokenReturns403() throws Exception {
        String token = JwtTestTokens.accessToken("1", "CUSTOMER");
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "New Item",
                                "description", "desc",
                                "price", new BigDecimal("9.99"),
                                "stockQuantity", 5,
                                "category", "Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void postWithAdminTokenCreatesProduct() throws Exception {
        String token = JwtTestTokens.accessToken("2", "ADMINISTRATOR");
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Admin Product",
                                "description", "from test",
                                "price", new BigDecimal("4.50"),
                                "stockQuantity", 2,
                                "category", "Integration"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Admin Product"));
    }

    @Test
    @Order(6)
    void putWithAdminUpdatesProduct() throws Exception {
        String token = JwtTestTokens.accessToken("2", "ADMINISTRATOR");
        mockMvc.perform(put("/api/products/b0000000-0000-4000-8000-000000000012")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "USB-C Cable Pro",
                                "description", "updated",
                                "price", new BigDecimal("15.00"),
                                "stockQuantity", 10,
                                "category", "Accessories"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USB-C Cable Pro"));
    }

    @Test
    @Order(7)
    void deleteWithAdminSoftDeletes() throws Exception {
        String token = JwtTestTokens.accessToken("2", "ADMINISTRATOR");
        String created = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "To Delete",
                                "description", "temp",
                                "price", new BigDecimal("1.00"),
                                "stockQuantity", 1,
                                "category", "Test"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(delete("/api/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void listRejectsMinPriceGreaterThanMaxPrice() throws Exception {
        mockMvc.perform(get("/api/products").param("minPrice", "100").param("maxPrice", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minPrice must be less than or equal to maxPrice"));
    }

    @Test
    @Order(9)
    void listSearchTreatsPercentAsLiteral() throws Exception {
        mockMvc.perform(get("/api/products").param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @Order(10)
    void listSearchFindsNameContainingPercentSign() throws Exception {
        String token = JwtTestTokens.accessToken("2", "ADMINISTRATOR");
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Smirnoff No.21 Red Label Vodka Bottle 37.5% Vol 1L",
                                "description", "Vodka",
                                "price", new BigDecimal("19.99"),
                                "stockQuantity", 3,
                                "category", "Alcohol"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products").param("q", "37.5%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem("Smirnoff No.21 Red Label Vodka Bottle 37.5% Vol 1L")));
    }
}
