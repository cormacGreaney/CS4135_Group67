package com.cs4135.group3.product_service.web;

import com.cs4135.group3.product_service.domain.Product;
import com.cs4135.group3.product_service.web.dto.ProductResponse;

// Turn a database Product into what we send to the browser (we don't expose deletedAt there).
public final class ProductMapper {

	private ProductMapper() {
	}

	public static ProductResponse toResponse(Product p) {
		return new ProductResponse(
				p.getId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getStockQuantity(),
				p.getCategory(),
				p.getCreatedAt(),
				p.isHasImage());
	}
}
