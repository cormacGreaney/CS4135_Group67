package com.cs4135.group3.user_service.service;

import com.cs4135.group3.user_service.config.JwtProperties;
import com.cs4135.group3.user_service.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class TokenService {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public TokenService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
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
