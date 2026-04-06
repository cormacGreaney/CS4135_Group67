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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// All rules for listing, loading, creating, updating, and "deleting" products.
@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	// Paged product list with optional search, category, and price range
	public Page<ProductResponse> list(
			Pageable pageable,
			String q,
			String category,
			BigDecimal minPrice,
			BigDecimal maxPrice) {
		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "minPrice must be less than or equal to maxPrice");
		}
		Specification<Product> spec = Specification.where(ProductSpecifications.notDeleted())
				.and(ProductSpecifications.nameContainsIgnoreCase(q))
				.and(ProductSpecifications.categoryEquals(category))
				.and(ProductSpecifications.priceAtLeast(minPrice))
				.and(ProductSpecifications.priceAtMost(maxPrice));
		return productRepository.findAll(spec, pageable).map(ProductMapper::toResponse);
	}

	// Single product for the product detail page (404 if missing or soft-deleted)
	public ProductResponse getById(UUID id) {
		return productRepository.findByIdAndDeletedAtIsNull(id)
				.map(ProductMapper::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
	}

	@Transactional
	public ProductResponse create(ProductCreateRequest req) {
		// New product from the admin form
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
		// Replace fields on an existing product
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
		// We don't remove the row from the database — we just mark it deleted
		p.setDeletedAt(Instant.now());
	}

	@Transactional(readOnly = true)
	public ProductImagePayload getImage(UUID id) {
		Product p = productRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		if (!p.isHasImage()) {
			throw new ResourceNotFoundException("Product image not found");
		}
		byte[] data = p.getImageData();
		if (data == null || data.length == 0) {
			throw new ResourceNotFoundException("Product image not found");
		}
		String contentType = p.getImageContentType();
		if (!StringUtils.hasText(contentType)) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		return new ProductImagePayload(data, contentType);
	}

	@Transactional
	public void replaceImage(UUID id, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
		}
		String contentType = file.getContentType();
		if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image (image/* content type)");
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		}
		catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read image file");
		}
		if (bytes.length == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty");
		}
		Product p = productRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		p.setImageData(bytes);
		p.setImageContentType(contentType);
		p.setHasImage(true);
	}

	@Transactional
	public void clearImage(UUID id) {
		Product p = productRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		p.setImageData(null);
		p.setImageContentType(null);
		p.setHasImage(false);
	}

	public record ProductImagePayload(byte[] data, String contentType) {
	}
}
