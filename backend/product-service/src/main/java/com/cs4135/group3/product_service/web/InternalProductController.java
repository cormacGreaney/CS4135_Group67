package com.cs4135.group3.product_service.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cs4135.group3.product_service.service.ProductService;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

	private final ProductService productService;

	public InternalProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping("/stock/deduct")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deductStock(@RequestBody DeductStockRequest request) {
		// Internal service-to-service API used by order-service after payment success.
		productService.deductStock(
				request.items().stream().map(item -> new ProductService.StockDeduction(item.productId(), item.quantity())).toList());
	}

	public record DeductStockRequest(Long orderId, List<DeductStockItemRequest> items) {
	}

	public record DeductStockItemRequest(UUID productId, Integer quantity) {
	}
}
