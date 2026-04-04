package com.cs4135.group3.product_service.repository;

import com.cs4135.group3.product_service.domain.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

// Small building blocks for "show me products where…". Combined in ProductService.list().
public final class ProductSpecifications {

	private ProductSpecifications() {
	}

	// Hide soft-deleted items from the catalog
	public static Specification<Product> notDeleted() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	// Search text: match if the name contains this string (ignores case). Empty means "no filter".
	// % and _ are escaped so the query is literal substring match.
	public static Specification<Product> nameContainsIgnoreCase(String q) {
		if (!StringUtils.hasText(q)) {
			return (root, query, cb) -> cb.conjunction();
		}
		String literal = q.trim().toLowerCase();
		String pattern = "%" + escapeLikeLiteral(literal) + "%";
		char escape = '\\';
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern, escape);
	}

	private static String escapeLikeLiteral(String s) {
		return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	// Exact category match (ignores case). Empty means "no filter".
	public static Specification<Product> categoryEquals(String category) {
		if (!StringUtils.hasText(category)) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
	}

	// Minimum price. Null means "no filter".
	public static Specification<Product> priceAtLeast(BigDecimal min) {
		if (min == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
	}

	// Maximum price. Null means "no filter".
	public static Specification<Product> priceAtMost(BigDecimal max) {
		if (max == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
	}
}
