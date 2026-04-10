package com.cs4135.group3.product_service.web;

import com.cs4135.group3.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

	private final ProductService productService;

	@PostMapping("/stock/deduct")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deductStock(@Valid @RequestBody DeductStockRequest request) {
		// This endpoint is for trusted service-to-service calls, not public storefront traffic.
		productService.deductStock(request.items().stream()
				.map(item -> new ProductService.StockDeduction(item.productId(), item.quantity()))
				.toList());
	}

    @PostMapping("/stock/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStock(@Valid @RequestBody AddStockRequest request) {
        // This endpoint is for trusted service-to-service calls, not public storefront traffic.
        productService.addStock(request.items().stream()
                .map(item -> new ProductService.StockAddition(item.productId(), item.quantity()))
                .toList());
    }

	public record DeductStockRequest(
			@NotEmpty List<@Valid DeductStockItemRequest> items) {
	}

    public record AddStockRequest(
            @NotEmpty List<@Valid AddStockItemRequest> items) {
    }

	public record DeductStockItemRequest(
			@NotNull UUID productId,
			@NotNull @Min(1) Integer quantity) {
	}

    public record AddStockItemRequest(
            @NotNull UUID productId,
            @NotNull @Min(1) Integer quantity) {
    }
}
