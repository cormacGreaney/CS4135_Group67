package com.cs4135.group3.product_service.repository;

import com.cs4135.group3.product_service.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

	Optional<Product> findByIdAndDeletedAtIsNull(UUID id);
}
