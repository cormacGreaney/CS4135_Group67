package com.cs4135.group3.order_service.integration;

import com.cs4135.group3.order_service.config.InternalApiTokenFilter;
import com.cs4135.group3.order_service.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductStockClient {

	private final RestClient restClient;

	@Value("${app.product-service.base-url}")
	private String productServiceBaseUrl;

	@Value("${app.internal-api.token}")
	private String internalApiToken;

	public void deductStock(List<OrderItem> items) {
		// Send only the minimal stock delta payload to product-service's internal endpoint.
		DeductStockRequest body = new DeductStockRequest(
				items.stream()
						.map(item -> new DeductStockItemRequest(item.getProductId(), item.getQuantity()))
						.toList());

		restClient.post()
				// Trim a trailing slash so config works with either http://host:port or http://host:port/.
				.uri(productServiceBaseUrl.replaceAll("/$", "") + "/internal/products/stock/deduct")
				.header(InternalApiTokenFilter.HEADER, internalApiToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.toBodilessEntity();
	}

	public record DeductStockRequest(List<DeductStockItemRequest> items) {
	}

	public record DeductStockItemRequest(UUID productId, Integer quantity) {
	}
}
