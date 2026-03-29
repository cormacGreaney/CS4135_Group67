package com.cs4135.group3.user_service.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException() {
		super("Email is already registered");
	}

}
