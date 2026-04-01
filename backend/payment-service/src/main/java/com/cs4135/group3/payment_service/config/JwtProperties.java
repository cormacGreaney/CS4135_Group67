package com.cs4135.group3.payment_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
