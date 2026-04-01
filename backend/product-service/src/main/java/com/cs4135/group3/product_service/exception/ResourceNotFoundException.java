package com.cs4135.group3.product_service.exception;

// Thrown when we look up a product id that doesn't exist or is already soft-deleted
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
