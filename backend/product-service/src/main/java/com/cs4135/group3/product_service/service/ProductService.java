package com.cs4135.group3.product_service.service;

import com.cs4135.group3.product_service.domain.Product;
import com.cs4135.group3.product_service.exception.ResourceNotFoundException;
import com.cs4135.group3.product_service.repository.ProductRepository;
import com.cs4135.group3.product_service.repository.ProductSpecifications;
import com.cs4135.group3.product_service.web.ProductMapper;
import com.cs4135.group3.product_service.web.dto.ProductCreateRequest;
import com.cs4135.group3.product_service.web.dto.ProductResponse;
import com.cs4135.group3.product_service.web.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Page<ProductResponse> list(
			Pageable pageable,
			String q,
			String category,
			BigDecimal minPrice,
			BigDecimal maxPrice) {
		Specification<Product> spec = Specification.where(ProductSpecifications.notDeleted())
				.and(ProductSpecifications.nameContainsIgnoreCase(q))
				.and(ProductSpecifications.categoryEquals(category))
				.and(ProductSpecifications.priceAtLeast(minPrice))
				.and(ProductSpecifications.priceAtMost(maxPrice));
		return productRepository.findAll(spec, pageable).map(ProductMapper::toResponse);
	}

	public ProductResponse getById(UUID id) {
		return productRepository.findByIdAndDeletedAtIsNull(id)
				.map(ProductMapper::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
	}

	@Transactional
	public ProductResponse create(ProductCreateRequest req) {
		Product p = new Product();
		p.setName(req.name().trim());
		p.setDescription(req.description() != null ? req.description().trim() : null);
		p.setPrice(req.price());
		p.setStockQuantity(req.stockQuantity());
		p.setCategory(req.category().trim());
		productRepository.save(p);
		return ProductMapper.toResponse(p);
	}

	@Transactional
	public ProductResponse update(UUID id, ProductUpdateRequest req) {
		Product p = productRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		p.setName(req.name().trim());
		p.setDescription(req.description() != null ? req.description().trim() : null);
		p.setPrice(req.price());
		p.setStockQuantity(req.stockQuantity());
		p.setCategory(req.category().trim());
		return ProductMapper.toResponse(p);
	}

	@Transactional
	public void softDelete(UUID id) {
		Product p = productRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		p.setDeletedAt(Instant.now());
	}
}
