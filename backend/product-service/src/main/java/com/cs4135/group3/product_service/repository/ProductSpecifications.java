package com.cs4135.group3.product_service.repository;

import com.cs4135.group3.product_service.domain.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class ProductSpecifications {

	private ProductSpecifications() {
	}

	public static Specification<Product> notDeleted() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<Product> nameContainsIgnoreCase(String q) {
		if (!StringUtils.hasText(q)) {
			return (root, query, cb) -> cb.conjunction();
		}
		String pattern = "%" + q.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
	}

	public static Specification<Product> categoryEquals(String category) {
		if (!StringUtils.hasText(category)) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
	}

	public static Specification<Product> priceAtLeast(BigDecimal min) {
		if (min == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
	}

	public static Specification<Product> priceAtMost(BigDecimal max) {
		if (max == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
	}
}
