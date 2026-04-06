package com.cs4135.group3.product_service;

import com.cs4135.group3.product_service.domain.Product;
import com.cs4135.group3.product_service.exception.ResourceNotFoundException;
import com.cs4135.group3.product_service.repository.ProductRepository;
import com.cs4135.group3.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void deductStockReducesAvailableQuantity() {
		UUID productId = UUID.randomUUID();
		Product product = new Product();
		product.setId(productId);
		product.setStockQuantity(5);
		when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

		productService.deductStock(List.of(new ProductService.StockDeduction(productId, 2)));

		assertEquals(3, product.getStockQuantity());
	}

	@Test
	void deductStockRejectsInsufficientQuantity() {
		UUID productId = UUID.randomUUID();
		Product product = new Product();
		product.setId(productId);
		product.setStockQuantity(1);
		when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> productService.deductStock(List.of(new ProductService.StockDeduction(productId, 2))));

		assertEquals(409, exception.getStatusCode().value());
		assertEquals(1, product.getStockQuantity());
	}

	@Test
	void deductStockRejectsMissingProduct() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> productService.deductStock(List.of(new ProductService.StockDeduction(productId, 1))));
	}
}
