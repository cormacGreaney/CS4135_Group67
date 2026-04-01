package com.cs4135.group3.product_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Helper for tests: make a login-style token. The secret has to match application-test.properties.
final class JwtTestTokens {

	private static final String SECRET = "test-secret-key-for-jwt-must-be-at-least-32-bytes-long!!";
	private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

	private JwtTestTokens() {
	}

	static String accessToken(String subject, String role) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + 3600_000L);
		return Jwts.builder()
				.subject(subject)
				.claim("email", subject + "@test.local")
				.claim("role", role)
				.issuedAt(now)
				.expiration(exp)
				.signWith(KEY)
				.compact();
	}
}
