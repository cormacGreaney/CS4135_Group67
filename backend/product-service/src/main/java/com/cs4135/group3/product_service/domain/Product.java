package com.cs4135.group3.product_service.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// One row in the products table. "Soft delete" means we set deletedAt instead of erasing the row.
@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@JdbcTypeCode(SqlTypes.UUID)
	@Column(nullable = false, columnDefinition = "BINARY(16)")
	// Stored as 16 bytes in MySQL; matches the Flyway migration
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(name = "stock_quantity", nullable = false)
	private int stockQuantity;

	@Column(nullable = false)
	private String category;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@JdbcTypeCode(SqlTypes.BLOB)
	@Column(name = "image_data", columnDefinition = "LONGBLOB")
	private byte[] imageData;

	@Column(name = "image_content_type", length = 100)
	private String imageContentType;

	@Column(name = "has_image", nullable = false)
	private boolean hasImage;

	// If this is set, the product is treated as deleted and won't show in the shop
	@Column(name = "deleted_at")
	private Instant deletedAt;

	// Set automatically when the row is first saved
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public String getImageContentType() {
		return imageContentType;
	}

	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@PrePersist
	void prePersist() {
		// First time we save, fill in createdAt if it wasn't set
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
