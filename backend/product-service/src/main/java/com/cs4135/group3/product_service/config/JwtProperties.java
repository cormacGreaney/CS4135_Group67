package com.cs4135.group3.product_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Reads app.jwt.secret and app.jwt.expiration-ms from application.properties / env.
// The secret should be the same as user-service so login tokens work here too.
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private String secret;
	private long expirationMs;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public void setExpirationMs(long expirationMs) {
		this.expirationMs = expirationMs;
	}
}
