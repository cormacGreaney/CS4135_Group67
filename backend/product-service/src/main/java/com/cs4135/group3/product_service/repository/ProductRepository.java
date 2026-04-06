package com.cs4135.group3.product_service.repository;

import com.cs4135.group3.product_service.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

// Database access for products. The "Specification" part is used for search filters on the list page.
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

	// Find by id only if the product is not soft-deleted
	Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Product> findWithLockByIdAndDeletedAtIsNull(UUID id);
}
