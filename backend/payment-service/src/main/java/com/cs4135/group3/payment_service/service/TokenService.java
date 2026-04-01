package com.cs4135.group3.payment_service.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.cs4135.group3.payment_service.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

	private final SecretKey secretKey;

	public TokenService(JwtProperties jwtProperties) {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public Authentication parseAuthentication(String token) throws JwtException {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		String userId = claims.getSubject();
		String role = claims.get("role", String.class);
		if (role == null || role.isBlank()) {
			throw new JwtException("Missing role claim");
		}
		var authority = new SimpleGrantedAuthority("ROLE_" + role);
		return new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
	}
}
