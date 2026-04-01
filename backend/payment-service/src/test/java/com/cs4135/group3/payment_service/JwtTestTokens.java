package com.cs4135.group3.payment_service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

final class JwtTestTokens {

	private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
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
