package com.cs4135.group3.order_service.integration;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;

@Component
public class ProductStockClient {

	private final RestTemplate restTemplate;

	@Value("${app.product-service.base-url:http://localhost:8082}")
	private String productServiceBaseUrl;

	@Value("${app.internal-api.token}")
	private String internalToken;

	public ProductStockClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void deductStock(Order order) {
		String url = productServiceBaseUrl.replaceAll("/$", "") + "/internal/products/stock/deduct";
		DeductStockRequest request = new DeductStockRequest(
				order.getId(),
				order.getOrderItems().stream().map(ProductStockClient::toLine).toList());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Internal-Token", internalToken);

		try {
			restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), Void.class);
		}
		catch (RestClientException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to update product stock", ex);
		}
	}

	private static DeductStockItemRequest toLine(OrderItem item) {
		return new DeductStockItemRequest(item.getProductId(), item.getQuantity());
	}

	public record DeductStockRequest(Long orderId, List<DeductStockItemRequest> items) {
	}

	public record DeductStockItemRequest(UUID productId, Integer quantity) {
	}
}
