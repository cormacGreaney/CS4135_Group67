package com.cs4135.group3.product_service.web;

import com.cs4135.group3.product_service.service.ProductService;
import com.cs4135.group3.product_service.web.dto.ProductCreateRequest;
import com.cs4135.group3.product_service.web.dto.ProductResponse;
import com.cs4135.group3.product_service.web.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

// HTTP API for the catalog. Clients usually call through the gateway at :8080, not this port directly.
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public Page<ProductResponse> list(
			@PageableDefault(size = 20, sort = "name") Pageable pageable,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice) {
		// Query params: q (name search), category, minPrice, maxPrice — all optional
		return productService.list(pageable, q, category, minPrice, maxPrice);
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable UUID id) {
		// One product by id
		return productService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody ProductCreateRequest body) {
		// Admin only (enforced in SecurityConfig)
		return productService.create(body);
	}

	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest body) {
		// Admin only
		return productService.update(id, body);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		// Admin only — marks deleted, does not wipe the row
		productService.softDelete(id);
	}
}
