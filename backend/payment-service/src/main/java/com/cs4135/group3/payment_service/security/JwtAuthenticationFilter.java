package com.cs4135.group3.payment_service.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cs4135.group3.payment_service.service.TokenService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenService tokenService;

	public JwtAuthenticationFilter(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(AUTHORIZATION);
		if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
			String raw = header.substring(BEARER_PREFIX.length()).trim();
			if (!raw.isEmpty()) {
				try {
					var auth = tokenService.parseAuthentication(raw);
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
				catch (JwtException ignored) {
					SecurityContextHolder.clearContext();
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
