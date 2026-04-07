package com.cs4135.group3.user_service;

import com.cs4135.group3.user_service.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Builds JWTs for security tests (wrong signature, expiry, bad claims).
 */
final class JwtTestTokens {

	private JwtTestTokens() {
	}

	static SecretKey keyFromSecret(String secret) {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/** Signed with a different secret than the running app. */
	static String signedWithWrongSecret() {
		SecretKey wrong = Keys.hmacShaKeyFor(
				"wrong-secret-wrong-secret-wrong-secret-wrong!!".getBytes(StandardCharsets.UTF_8));
		Date now = new Date();
		return Jwts.builder()
				.subject("1")
				.claim("role", "CUSTOMER")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 3600_000))
				.signWith(wrong)
				.compact();
	}

	/** Valid signature but exp in the past. */
	static String expired(JwtProperties jwtProperties) {
		SecretKey key = keyFromSecret(jwtProperties.getSecret());
		Date exp = new Date(System.currentTimeMillis() - 60_000);
		Date iat = new Date(exp.getTime() - 3600_000);
		return Jwts.builder()
				.subject("1")
				.claim("role", "CUSTOMER")
				.claim("email", "x@y.com")
				.issuedAt(iat)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	/** Valid signature and not expired, but missing {@code role} claim (parseAuthentication rejects). */
	static String missingRoleClaim(JwtProperties jwtProperties) {
		SecretKey key = keyFromSecret(jwtProperties.getSecret());
		Date now = new Date();
		return Jwts.builder()
				.subject("1")
				.claim("email", "x@y.com")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 3600_000))
				.signWith(key)
				.compact();
	}
}
